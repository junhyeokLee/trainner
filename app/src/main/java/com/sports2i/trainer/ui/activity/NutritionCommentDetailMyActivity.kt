package com.sports2i.trainer.ui.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.sports2i.trainer.R
import com.sports2i.trainer.data.model.GroupInfo
import com.sports2i.trainer.data.model.GroupUser
import com.sports2i.trainer.data.model.NutritionDirectionKeyword
import com.sports2i.trainer.data.model.NutritionDirectionSearchResponse
import com.sports2i.trainer.data.model.NutritionPictureUser
import com.sports2i.trainer.databinding.ActivityNutritionCommentDetailMyBinding
import com.sports2i.trainer.ui.adapter.groupmember.GroupMemberNutritionCommentAdapter
import com.sports2i.trainer.ui.adapter.myexercise.NutritionPictureDetailAdapter
import com.sports2i.trainer.ui.adapter.myexercise.NutritionPictureDetailSearchAdapter
import com.sports2i.trainer.ui.dialog.CustomNutiritionImageDialogFragment
import com.sports2i.trainer.ui.view.DateSelectionView
import com.sports2i.trainer.utils.DateTimeUtil
import com.sports2i.trainer.utils.FileUtil
import com.sports2i.trainer.utils.FileUtil.extractFileName
import com.sports2i.trainer.utils.FileUtil.rotateBitmap
import com.sports2i.trainer.utils.FileUtil.saveBitmapAndGetUri
import com.sports2i.trainer.utils.Global
import com.sports2i.trainer.utils.NCPImageDelete
import com.sports2i.trainer.utils.NCPImageUpload
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.utils.Preferences
import com.sports2i.trainer.viewmodel.GroupViewModel
import com.sports2i.trainer.viewmodel.NutritionViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class NutritionCommentDetailMyActivity : BaseActivity<ActivityNutritionCommentDetailMyBinding>({ActivityNutritionCommentDetailMyBinding.inflate(it)}){

    private val groupViewModel: GroupViewModel by viewModels()
    private val nutritionViewModel: NutritionViewModel by viewModels()

    private var userList: MutableList<GroupUser> = mutableListOf()
    private var groupInfoList: MutableList<GroupInfo> = mutableListOf()
    private var keywordList: MutableList<NutritionDirectionKeyword> = mutableListOf()

    private var nutritionDirectionSearch: NutritionDirectionSearchResponse.DirectionData? = null

    private lateinit var nutritionCommentAdapter : GroupMemberNutritionCommentAdapter
    private lateinit var nutritionPictureDetailAdater: NutritionPictureDetailAdapter // 등록할 이미지
    private lateinit var nutritionPictureDetailSearchAdater: NutritionPictureDetailSearchAdapter // api 받아온 이미지

    var selectedPositions: Set<Int> = setOf()
    var nutritionPictureUserList : MutableList<NutritionPictureUser> = mutableListOf()

    var pictureImageList: MutableList<String> = mutableListOf()
    var currentPhotoPath: String = ""

    var selectedUserId : String? = null
    var selectedDateTime: String? = ""

    private val REQUEST_CODE_PICK_IMAGE = 100
    private val REQUEST_CODE_CAPTURE_IMAGE = 101
    private val MY_CAMERA_PERMISSION_CODE = 1001

    private var dateSelectionView : DateSelectionView? = null

    private var editMode = false
    private var editSearchMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        next()
        init()
        setContent()
        setFunction()
        observe()
        clickBack()
        binding.root.post { setLiveData() }
        this@NutritionCommentDetailMyActivity.onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PICK_IMAGE -> {
                    // 앨범에서 선택한 이미지를 처리하는 로직
                    val selectedImages = mutableListOf<String>()
                    if (data?.clipData != null) {
                        // 다중 이미지 선택
                        for (i in 0 until data.clipData!!.itemCount) {
                            val uri = data.clipData!!.getItemAt(i).uri
                            val compressedUri = FileUtil.compressAndSaveImage(this, uri)
                            selectedImages.add(compressedUri.toString())
                        }
                    } else {
                        // 단일 이미지 선택
                        val selectedImageUri = data?.data
                        selectedImageUri?.let {
                            val compressedUri = FileUtil.compressAndSaveImage(this, it)
                            selectedImages.add(compressedUri.toString())
                        }
                    }
                    // 선택한 이미지를 리사이클러뷰에 추가하는 작업 수행
                    selectedImages.forEach { uri ->
                        nutritionPictureDetailAdater.addData(uri)
                    }

                    checkedImageVisible()
                    dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate())
                }
                REQUEST_CODE_CAPTURE_IMAGE -> {
                    // 카메라로 찍은 이미지를 처리하는 로직
                var file = File(currentPhotoPath)
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))
                    // Exif 정보를 확인하여 이미지 회전 처리
                    val exif = ExifInterface(currentPhotoPath)
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)
                    val rotatedBitmap = rotateBitmap(orientation, bitmap)
                    // 회전된 이미지를 저장하고 그 Uri를 얻음
                    val rotatedImageUri = saveBitmapAndGetUri(this, rotatedBitmap)
                    Log.e("rotatedImageUri", rotatedImageUri.toString())
                    // 이미지 압축 및 크기 조절 후 리사이클러뷰에 추가
                    rotatedImageUri?.let {
                        nutritionPictureDetailAdater.addData(it.toString())
                    } ?: run {
                        Global.showBottomSnackBar(binding.root, "이미지를 불러오는데 실패했습니다.")

                    }
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    checkedImageVisible()
                    dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate())
                }
            }
        }
    }

    private fun init() {
        Preferences.init(this, Preferences.DB_USER_INFO)
        selectedUserId = intent.getStringExtra("userId")
        selectedDateTime = intent.getStringExtra("dateTime")
        this.groupViewModel.requestGroupInfo(Preferences.string(Preferences.KEY_ORGANIZATION_ID))
    }

    private fun setLiveData(){
        dateSelectionView = DateSelectionView(this)
        binding.dateSelectionView.apply {
            addView(dateSelectionView)
        }
        dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate())
        dateSelectionView!!.selectedDate.observe(this, ::handleSelectedDate)
    }
    private fun handleSelectedDate(selectedDate: String) {
        selectedDateTime = DateTimeUtil.formatDateSelection(selectedDate)
        this.groupViewModel.requestGroupUser(selectedUserId!!)
        this.nutritionViewModel.searchNutritionDirection(selectedUserId!!, selectedDateTime!!)
        this.nutritionViewModel.searchNutritionPicture(selectedUserId!!, selectedDateTime!!)
    }

    private fun setContent() {

        nutritionCommentAdapter = GroupMemberNutritionCommentAdapter(keywordList)
        nutritionPictureDetailAdater = NutritionPictureDetailAdapter(pictureImageList)
        nutritionPictureDetailSearchAdater = NutritionPictureDetailSearchAdapter(nutritionPictureUserList)

        binding.rvNutritionComment.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = nutritionCommentAdapter
        }

        binding.rvNutritionImage.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
            adapter = nutritionPictureDetailAdater
        }

        binding.rvSearchNutritionImage.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
            adapter = nutritionPictureDetailSearchAdater

        }

    }

    private fun setFunction() {
        refreshing()

        pictureDetailClicked()
        pictureDetailSearchClicked()

        binding.btnNutritionEnroll.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this@NutritionCommentDetailMyActivity)
            dialogBuilder
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.nutrition_images_enroll))
                .setPositiveButton("확인") { dialog, which ->
                    uploadNutritionImage()
                }
                .setNegativeButton("취소") {dialog, which -> dialog.cancel() }
            dialogBuilder.show()
        }
    }

    private fun refreshing() {
        binding.refreshLayout?.setOnRefreshListener {
            binding.refreshLayout?.isRefreshing = false
            dateSelectionView!!.setSelectedDateExternal(selectedDateTime ?: DateTimeUtil.getCurrentDate())
        }
    }

    private fun uploadNutritionImage(){
        val endPoint = Global.endPoint
        val bucketName = Global.bucketName+"/foods"
        var nutritionPictureList : MutableList<NutritionPictureUser> = mutableListOf()

        if(nutritionPictureDetailAdater.itemCount == 0) return
        val imageList = nutritionPictureDetailAdater.getImageList()
        imageList.forEach { imageUri ->
            try {
                NCPImageUpload(this, imageUri,"foods").execute()
                // NCPImage Upload 후에 이미지 NCP 업로드된 URL을 얻어옴
                val file = File(imageUri)
                var uploadUrl = endPoint + "/" + bucketName + "/" + file.name
                val nutritionPicture = NutritionPictureUser(
                    nutritionId = 0,
                    userId = Preferences.string(Preferences.KEY_USER_ID),
                    userName = Preferences.string(Preferences.KEY_USER_NAME),
                    pictureUrl = uploadUrl,
                    reportingDate = selectedDateTime!!,
                    evaluation = 0
                )
                nutritionPictureList.add(nutritionPicture)
                // 이미지를 업로드
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        Log.e("TAG", "uploadNutritionImage: $nutritionPictureList")
        nutritionViewModel.insertNutritionPicture(nutritionPictureList)
        nutritionPictureDetailAdater.clearData()
        nutritionPictureList.clear()
        Global.showBottomSnackBar(binding.root, "식단제출 완료.")

        checkedImageVisible()

        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
        back()

    }

    private inline fun <reified T : Any> observeNetworkState(
        stateLiveData: LiveData<NetworkState<T>>,
        crossinline onSuccess: (NetworkState.Success<T>) -> Unit,
        crossinline onError: (NetworkState.Error) -> Unit
    ) {
        stateLiveData.observe(this) { state ->
            when (state) {
                is NetworkState.Success -> {
                    onSuccess(state)
                    Handler(mainLooper).postDelayed({ handlerLoading(false) }, 200)
                }
                is NetworkState.Error -> {
                    handlerError(state.message)
                    onError(state)
                }
                is NetworkState.Loading -> {
                    handlerLoading(state.isLoading)
                }
            }
        }
    }

    private fun observe(){
        // 네트워크 상태 관찰자를 합쳐서 로딩 상태를 처리합니다.
        val networkStateObservers = listOf(
            ::observeGroupInfo,
            ::observeGroupUser,
            ::observeSearchNutritionDirection,
            ::observeSearchNutritionPicture
        )

        networkStateObservers.forEach { observer ->
            observer.invoke()
        }
    }

    private fun observeGroupInfo() { observeNetworkState(
        this.groupViewModel.groupInfoState,{
               handlerGroupInfoSuccess(it.data.data)
        },{Log.e("TAG", "Error: ${it.message}")})
    }

    private fun observeGroupUser() { observeNetworkState(
        this.groupViewModel.groupUserState,{
            handlerGroupUserSuccess(it.data.data)
        },{Log.e("TAG", "Error: ${it.message}")})
    }

    private fun observeSearchNutritionDirection() { observeNetworkState(
        this.nutritionViewModel.searchNutritionDirectionSearchSatate,{
            handlerNutritionDirectionSearchSuccess(it.data.data)
        },{Log.e("TAG", "Error: ${it.message}")})
    }

    private fun observeSearchNutritionPicture() { observeNetworkState(
        this.nutritionViewModel.searchNutritionPictureState,{
            handlerNutritionSearchPictureSuccess(it.data.data)
        },{Log.e("TAG", "Error: ${it.message}")})
    }

    private fun handlerGroupInfoSuccess(groupInfos: MutableList<GroupInfo>) {
        groupInfoList = groupInfos
    }

    private fun handlerGroupUserSuccess(groupUsers: MutableList<GroupUser>) {
        userList = groupUsers
    }

    private fun handlerNutritionSearchPictureSuccess(nutritionPicture: MutableList<NutritionPictureUser>) {
        nutritionPictureUserList.clear()
        nutritionPictureUserList.addAll(nutritionPicture)
        checkedImageVisible()

    }


    private fun handlerNutritionDirectionSearchSuccess(nutritionDirection: NutritionDirectionSearchResponse.DirectionData) {
//        keywordList.clear()
        nutritionDirectionSearch = nutritionDirection

        if(nutritionDirectionSearch?.keywordList!!.isEmpty()){
            binding.tvNutritionCommentEmpty.visibility = View.VISIBLE
            binding.rvNutritionComment.visibility = View.GONE
        } else {
            binding.tvNutritionCommentEmpty.visibility = View.GONE
            binding.rvNutritionComment.visibility = View.VISIBLE
        }

        keywordList = nutritionDirectionSearch?.keywordList!!
        binding.tvNutritionComment.text = nutritionDirectionSearch?.content
        binding.rvNutritionComment.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            nutritionCommentAdapter = GroupMemberNutritionCommentAdapter(keywordList)
            adapter = nutritionCommentAdapter

        }

    }

    private fun pictureDetailClicked(){
            nutritionPictureDetailAdater.mListener =
                object : NutritionPictureDetailAdapter.OnItemClickListener {
                    override fun onNutritionGroupMemberStatusClicked(
                        position: Int,
                        imageList: MutableList<String>
                    ) {
                        if (!editMode) {
                            Glide.with(binding.ivNutritionImage)
                                .load(imageList[position])
                                .transform(CenterCrop(), RoundedCorners(24)) // 라운드 처리
                                .error(R.drawable.ic_empty_food) // 에러 시 디폴트 이미지 표시
                                .into(binding.ivNutritionImage)
                        }
                    }

                    override fun onNutritionGroupMemberStatusLongClicked(
                        position: Int,
                        imageList: MutableList<String>,
                        nutritionCheck: Boolean
                    ) {
                        if (nutritionCheck) {
                            binding.tvDeletePicture.visibility = View.VISIBLE
                            editMode = true
                        } else {
                            binding.tvDeletePicture.visibility = View.GONE
                            editMode = false
                        }

                        binding.tvDeletePicture.setOnClickListener {
                            if (nutritionPictureDetailAdater.getSelectedPositions().isEmpty()) {
                                Global.showBottomSnackBar(binding.root, "선택된 사진이 없습니다.")
                                return@setOnClickListener
                            }

                            val selectedPositions =
                                nutritionPictureDetailAdater.getSelectedPositions()

                            for (selectedPosition in selectedPositions) {
                                val selectedImage =
                                    nutritionPictureDetailAdater.getItem(selectedPosition)
                                // 만약 selectedImage가 null이 아니라면 deleteData 호출
                                selectedImage?.let {
                                    nutritionPictureDetailAdater.deleteData(it)
                                }
                            }
                            checkedImageVisible()
                        }
                    }

                    override fun onAddPictureClicked() {
                        val options = arrayOf<CharSequence>("카메라로 촬영", "앨범에서 선택", "취소")
                        val builder =
                            AlertDialog.Builder(this@NutritionCommentDetailMyActivity)
                        builder.setTitle("사진 추가")
                        builder.setItems(options) { _, which ->
                            when (which) {
                                0 -> openCamera()
                                1 -> openGallery()
                                // 취소 버튼을 누른 경우
                                2 -> {}
                            }
                        }
                        builder.show()
                    }
                }
             }

    private fun pictureDetailSearchClicked() {
            nutritionPictureDetailSearchAdater.mListener =
                object : NutritionPictureDetailSearchAdapter.OnItemClickListener {
                    override fun onNutritionDetailSearchStatusClicked(
                        position: Int,
                        imageList: MutableList<NutritionPictureUser>
                    ) {
                        if(!editSearchMode) {
                            val dialogFragment = CustomNutiritionImageDialogFragment.newInstance(
                                imageList[position].pictureUrl,
                                imageList[position].evaluation.toString()
                            )
                            dialogFragment.show(
                                supportFragmentManager,
                                CustomNutiritionImageDialogFragment.TAG
                            )
                        }
                    }

                    override fun onNutritionDetailSearchStatusLongClicked(
                        position: Int,
                        imageList: MutableList<NutritionPictureUser>,
                        nutritionCheck: Boolean
                    ) {
                        if (nutritionCheck) {
                            binding.tvSearchDeletePicture.visibility = View.VISIBLE
                            editSearchMode = true
                        } else {
                            binding.tvSearchDeletePicture.visibility = View.GONE
                            editSearchMode = false
                        }


                        var nutritionId = mutableListOf<Int>()

                        binding.tvSearchDeletePicture.setOnClickListener {
                            if (nutritionPictureDetailSearchAdater.getSelectedPositions()
                                    .isEmpty()
                            ) {
                                Global.showBottomSnackBar(binding.root, "선택된 사진이 없습니다.")

                                return@setOnClickListener
                            }

                            val selectedPositions =
                                nutritionPictureDetailSearchAdater.getSelectedPositions()

                            for (selectedPosition in selectedPositions) {
                                val selectedImage =
                                    nutritionPictureDetailSearchAdater.getItem(selectedPosition)
                                // 만약 selectedImage가 null이 아니라면 deleteData 호출
                                selectedImage?.let {
                                    nutritionPictureDetailSearchAdater.deleteData(it)
                                    var nutritionName = extractFileName(it.pictureUrl)
                                    Log.e("TAG", "nutritionName: $nutritionName")
                                    Log.e("TAG", "it.nutritionId: ${it.pictureUrl}")
                                    val deleteTask = NCPImageDelete(
                                        this@NutritionCommentDetailMyActivity,
                                        nutritionName
                                    )
                                    deleteTask.execute()
                                    nutritionId.add(it.nutritionId)
                                }
                            }
                            nutritionViewModel.deleteNutritionPicture(nutritionId)

                            // 데이터 갱신 후 뷰 갱신
                            nutritionPictureDetailSearchAdater.notifyDataSetChanged()

                            checkedImageVisible()
                        }
                    }
                }
            }


    private fun openCamera() {
        // 카메라 권한을 확인하고 없으면 요청
        if (ContextCompat.checkSelfPermission(this@NutritionCommentDetailMyActivity, Manifest.permission.CAMERA) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@NutritionCommentDetailMyActivity, arrayOf(Manifest.permission.CAMERA), MY_CAMERA_PERMISSION_CODE)
        } else {
            dispatchTakePictureIntent()
        }
    }

    private fun openGallery() {
        // 갤러리를 열기 위한 코드 추가
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // 여러 이미지 선택을 허용
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
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
                    startActivityForResult(takePictureIntent, REQUEST_CODE_CAPTURE_IMAGE)
                }
            }
        }
    }


    private fun checkedImageVisible() {

        if(nutritionPictureDetailAdater.getImageList().isNotEmpty()){
            binding.btnNutritionEnroll.isEnabled = true
            binding.ivNutritionImage.visibility = View.VISIBLE
            binding.tvNutritionTime.visibility = View.VISIBLE
            binding.nutritionEmptyLayout.visibility = View.INVISIBLE
        }
        else{
            binding.btnNutritionEnroll.isEnabled = false
            binding.ivNutritionImage.visibility = View.INVISIBLE
            binding.tvNutritionTime.visibility = View.GONE
            binding.nutritionEmptyLayout.visibility = View.VISIBLE
            binding.tvDeletePicture.visibility = View.GONE
        }

        if(nutritionPictureDetailSearchAdater.getImageList().isEmpty()) binding.tvSearchDeletePicture.visibility = View.GONE
        else binding.tvSearchDeletePicture.visibility = View.VISIBLE


        if(nutritionPictureUserList.isEmpty()) {
            binding.rvSearchNutritionImage.visibility = View.GONE
            binding.tvNutritionExitEmpty.visibility = View.VISIBLE
        }
        else {
            binding.rvSearchNutritionImage.visibility = View.VISIBLE
            binding.tvNutritionExitEmpty.visibility = View.GONE
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
    private fun handlerLoading(isLoading: Boolean) {
        if(_binding != null) {
            if (isLoading) {
                binding.llProgressBar.visibility = View.VISIBLE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.llProgressBar.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // "뒤로 가기" 버튼 처리
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
