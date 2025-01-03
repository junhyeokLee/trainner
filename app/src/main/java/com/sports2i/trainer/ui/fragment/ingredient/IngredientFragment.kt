package com.sports2i.trainer.ui.fragment.ingredient

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sports2i.trainer.data.model.Ingredient
import com.sports2i.trainer.databinding.FragmentIngredientBinding
import com.sports2i.trainer.ui.adapter.ingredient.IngredientListAdapter
import com.sports2i.trainer.ui.dialog.CustomIngredientDialogFragment
import com.sports2i.trainer.ui.fragment.BaseFragment
import com.sports2i.trainer.utils.NetworkState
import com.sports2i.trainer.viewmodel.IngredientViewModel
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class IngredientFragment : BaseFragment<FragmentIngredientBinding>() {

    companion object {
        private const val ITEMS_PER_PAGE = 20
    }
    private val fragmentIngredientBinding
        get() = binding!!

    private lateinit var ingredientListAdapter: IngredientListAdapter
    private val ingredientViewModel: IngredientViewModel by viewModels()

    private var ingredientList: MutableList<Ingredient> = mutableListOf()
    private var ingredient: Ingredient? = null
    private var version = ""
    private var lastIndex = 0
    private var category = ""
    private var isCheckedMedicine = false
    private var isCheckedMedicineNon = false

    private var isLoadingData = false // 추가된 부분

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentIngredientBinding {
        return FragmentIngredientBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setContent()
        setFunction()
        networkStatus()

    }
    override fun setContent() {
        showBottomNavigation()
        hideTopBar()

        ingredientViewModel.getIngredient()

        ingredientListAdapter = IngredientListAdapter(ingredientList)
        fragmentIngredientBinding.rvMedicine.apply {
            layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            adapter = ingredientListAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPositions(null).maxOrNull()

                    // 스크롤이 마지막 아이템에 도달하면 추가 데이터를 불러오기
                    if (!isLoadingData && lastVisibleItemPosition == ingredientList.size - 1) {
                        Log.e("lastIndex", lastIndex.toString())
                        loadMoreData()
                    }
                }
            })
        }

        if(ingredientList.isEmpty()) {
            fragmentIngredientBinding.tvEmpty.visibility = View.VISIBLE
            fragmentIngredientBinding.rvMedicine.visibility = View.GONE
        } else {
            fragmentIngredientBinding.tvEmpty.visibility = View.GONE
            fragmentIngredientBinding.rvMedicine.visibility = View.VISIBLE
        }

    }

    override fun setFunction() {

        fragmentIngredientBinding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(editable: Editable?) {
                // 검색어가 변경될 때마다 호출되는 부분
                val keyword = editable.toString()
                performSearch(keyword)
            }
        })

        ingredientListAdapter.mListener = object : IngredientListAdapter.OnItemClickListener {
            override fun onIngredientClicked(position: Int, ingredient: Ingredient) {

                val positiveButtonClickListener = DialogInterface.OnClickListener { dialog, _ ->
                    dialog.dismiss()
                }
                val dialogFragment = CustomIngredientDialogFragment.newInstance(positiveButtonClickListener, ingredient)
                dialogFragment.show(childFragmentManager, CustomIngredientDialogFragment.TAG)
            }
        }

        fragmentIngredientBinding.medicineCheckBox.setOnCheckedChangeListener { _, isChecked ->
                performSearch(fragmentIngredientBinding.etSearch.text.toString())
        }

        fragmentIngredientBinding.medicineNonCheckBox.setOnCheckedChangeListener { _, isChecked ->
                performSearch(fragmentIngredientBinding.etSearch.text.toString())
            }
    }

    private fun performSearch(keyword: String) {
        category = when {
            fragmentIngredientBinding.medicineCheckBox.isChecked -> "의약품"
            fragmentIngredientBinding.medicineNonCheckBox.isChecked -> "의약외품"
            else -> ""
        }
        if (category.isNotEmpty()) {
            clearAndLoadData(keyword)
        }
    }

    private fun clearAndLoadData(keyword: String) {
        ingredientList.clear()
        if(keyword.isEmpty()) {
            fragmentIngredientBinding.tvEmpty.visibility = View.VISIBLE
            fragmentIngredientBinding.rvMedicine.visibility = View.GONE
        } else {
            fragmentIngredientBinding.tvEmpty.visibility = View.GONE
            fragmentIngredientBinding.rvMedicine.visibility = View.VISIBLE
        }

        ingredientViewModel.getIngredientList(version, category, keyword, 0, ITEMS_PER_PAGE)

    }

    private fun loadMoreData() {
        isLoadingData = true
        showLoading()

        val lastNoticeId = ingredientList.lastOrNull()?.drugId?.itemSeq ?: 0
        val keyword = fragmentIngredientBinding.etSearch.text.toString()
        lastIndex = lastNoticeId
        ingredientViewModel.getIngredientList(version, category, keyword, lastIndex, ITEMS_PER_PAGE)
    }

    override fun refreshing() {}

    override fun networkStatus() {
        ingredientViewModel.ingredientState.observe(viewLifecycleOwner){
            when(it){
                is NetworkState.Success -> handlerIngredientSuccess(it.data.data!!)
                is NetworkState.Error -> handlerError(it.message)
                is NetworkState.Loading -> handlerLoading()
            }
        }

        ingredientViewModel.ingredientListState.observe(viewLifecycleOwner){
            when(it){
                is NetworkState.Success -> {
                    handlerIngredientListSuccess(it.data.data)
                    hideLoading()
                }
                is NetworkState.Error -> handlerError(it.message)
                is NetworkState.Loading -> handlerLoading()
            }
        }
    }

    private fun handlerIngredientSuccess(it: Ingredient) {

        if (it == null) {
            Log.e("TAG", "null: $it")
        } else {
            Log.e("TAG", "not null: $it")
            ingredient = it
            version = ingredient!!.drugId.version
        }
    }

    private fun handlerIngredientListSuccess(it: MutableList<Ingredient>) {
        val uniqueIngredientSet = HashSet<Int>()
        val uniqueIngredients = it.filterNot { newIt ->
            !uniqueIngredientSet.add(newIt.drugId.itemSeq)
        }
//        val uniqueIngredient = it.filterNot { newIt ->
//            ingredientList.any { ingredient -> ingredient.drugId.itemSeq == newIt.drugId.itemSeq }
//        }

        ingredientList.addAll(uniqueIngredients)

        isLoadingData = false

        if(ingredientList.isEmpty()) {
            fragmentIngredientBinding.tvEmpty.visibility = View.VISIBLE
            fragmentIngredientBinding.rvMedicine.visibility = View.GONE
        } else {
            fragmentIngredientBinding.tvEmpty.visibility = View.GONE
            fragmentIngredientBinding.rvMedicine.visibility = View.VISIBLE
        }

        ingredientListAdapter.notifyDataSetChanged()
    }
    private fun handlerError(errorMessage: String?) {
        errorMessage?.let {
            Log.e("TAG", "Error: $it")
        }
    }

    private fun handlerLoading() {
//        Global.progressON(requireContext())
    }

    private fun showLoading(){
        fragmentIngredientBinding.progressBar.visibility = View.VISIBLE
    }
    private fun hideLoading(){
        fragmentIngredientBinding.progressBar.visibility = View.GONE
    }
    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }


}