package com.sports2i.trainer.ui.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.UserResponse
import com.sports2i.trainer.databinding.ActivityEditProfileBinding
import com.sports2i.trainer.ui.dialog.CustomPasswordChangeDialog
import com.sports2i.trainer.ui.dialog.CustomRecordDialogFragment
import com.sports2i.trainer.utils.FileUtil
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class EditProfileActivity : BaseActivity<ActivityEditProfileBinding>({ActivityEditProfileBinding.inflate(it)}) {

    private val groupViewModel: GroupViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private val CAMERA_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 2
    private val MY_CAMERA_PERMISSION_CODE = 102
    private var currentPhotoPath: String = ""

    private var userId = ""
    private var email = ""
    private var userName = ""
    private var authority = ""
    private var groupId = ""

    private lateinit var userInfo : UserResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        observe()
        clickBack()

        this@EditProfileActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
//                    val selectedImageUri: Uri? = data?.data
//                    selectedImageUri?.let { handleSelectedImage(it) }

                    val selectedImageUri = data?.data
                    selectedImageUri?.let {
                        val compressedUri = FileUtil.compressAndSaveImage(this, it)
                        handleSelectedImage(compressedUri.toString())
                    }
                }

                CAMERA_REQUEST_CODE -> {
//                    val capturedImage: Bitmap = data?.extras?.get("data") as Bitmap
//                    handleCapturedImage(capturedImage)

                    var file = File(currentPhotoPath)
                    try {
                        val bitmap =
                            MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))
                        // Exif 정보를 확인하여 이미지 회전 처리
                        val exif = ExifInterface(currentPhotoPath)
                        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                        val rotatedBitmap = FileUtil.rotateBitmap(orientation, bitmap)
                        // 회전된 이미지를 저장하고 그 Uri를 얻음
                        val rotatedImageUri = FileUtil.saveBitmapAndGetUri(this, rotatedBitmap)

                        // 이미지 압축 및 크기 조절 후 리사이클러뷰에 추가
                        rotatedImageUri?.let {
                            handleCapturedImage(it.toString())
//                            nutritionPictureDetailAdater.addData(it.toString())
                        } ?: run {
                            Global.showBottomSnackBar(binding.root, "이미지를 불러오는데 실패했습니다")
                        }
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    private fun init() {
        Preferences.init(this, Preferences.DB_USER_INFO)
        userId = Preferences.string(Preferences.KEY_USER_ID)
        email = Preferences.string(Preferences.KEY_EMAIL)
        userName = Preferences.string(Preferences.KEY_USER_NAME)
        authority = Preferences.string(Preferences.KEY_AUTHORITY)
        groupId = Preferences.string(Preferences.KEY_GROUP_ID)

        binding.tvMyName.text = Preferences.string(Preferences.KEY_USER_NAME)

        userViewModel.getUserInfo(userId)

        val cancelClickListener = DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss()
        }

        val saveClickListener = DialogInterface.OnClickListener { dialog, _ ->
            Intent(this, LoginActivity::class.java).apply {
                startActivity(this)
                finish()
            }
            dialog.dismiss()
        }

        binding.editPwd.setOnClickListener {
            val dialogFragment = CustomPasswordChangeDialog.newInstance(
                cancelClickListener, saveClickListener
            )

            dialogFragment.show(supportFragmentManager, CustomRecordDialogFragment.TAG)
        }

        binding.tvProfileEdit.setOnClickListener {
            showImageSelectionDialog()
        }
    }

    private fun observe() {

        this.groupViewModel.requestGroupInfo(Preferences.string(Preferences.KEY_ORGANIZATION_ID))

        this.userViewModel.userState.observe(this@EditProfileActivity) {
            when (it) {
                is NetworkState.Success -> { handlerUserSuccess(it.data.data) }
                is NetworkState.Error -> handlerError(it.message)
                is NetworkState.Loading -> handlerLoading()
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun handlerUserSuccess(userInfo:UserResponse){
        this.userInfo = userInfo

        if(userInfo.profileUrl.isNullOrEmpty()) {
            binding.logoImg.visibility = View.VISIBLE
            binding.ivProfileImg.visibility = View.GONE
        }else{

            binding.logoImg.visibility = View.GONE
            binding.ivProfileImg.visibility = View.VISIBLE

            Glide.with(this)
                .load(this.userInfo.profileUrl)
                .transform(CenterCrop(), RoundedCorners(24)) // 라운드 처리
                .error(com.google.android.material.R.drawable.mtrl_ic_error) // 에러 시 디폴트 이미지 표시
                .into(binding.ivProfileImg)
        }
    }

    private fun handlerError(errorMessage: String?) {
        errorMessage?.let {
            Log.e("TAG", "Error: $it")
        }
    }

    private fun handlerLoading() {
//        Global.progressON(requireContext())
    }

    private fun showImageSelectionDialog() {
        val items = arrayOf("카메라로 촬영", "앨범에서 선택", "취소")
        AlertDialog.Builder(this)
            .setTitle("프로필 사진 선택")
            .setItems(items) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                    2-> {}
                }
            }
            .show()
    }

    private fun openCamera() {
        // 카메라 권한을 확인하고 없으면 요청
        if (ContextCompat.checkSelfPermission(this@EditProfileActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@EditProfileActivity, arrayOf(
                Manifest.permission.CAMERA), MY_CAMERA_PERMISSION_CODE)
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    private fun handleSelectedImage(selectedImage: String) {
        binding.logoImg.visibility = android.view.View.GONE
        binding.ivProfileImg.visibility = android.view.View.VISIBLE

        Glide.with(this)
            .load(selectedImage)
            .transform(CenterCrop(), RoundedCorners(24)) // 라운드 처리
            .error(R.drawable.ic_profile) // 에러 시 디폴트 이미지 표시
            .into(binding.ivProfileImg)

        Log.e("selectedImage",""+selectedImage)

        uploadProfileImage(selectedImage)
    }

    private fun handleCapturedImage(capturedImage: String) {
        binding.logoImg.visibility = android.view.View.GONE
        binding.ivProfileImg.visibility = android.view.View.VISIBLE

        Glide.with(this)
            .load(capturedImage)
            .transform(CenterCrop(), RoundedCorners(24)) // 라운드 처리
            .error(R.drawable.ic_profile) // 에러 시 디폴트 이미지 표시
            .into(binding.ivProfileImg)


        uploadProfileImage(capturedImage)

    }


    private fun uploadProfileImage(image: String) {
        val file = File(image)
        userViewModel.requestProfileUpdate(userId,file)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.sports2i.trainer.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                }
            }
        }
    }


    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            finish()
            back()
        }
    }
    private fun clickBack() {
        binding.layoutBack.setOnClickListener {
            finish()
            back()
        }
    }




}