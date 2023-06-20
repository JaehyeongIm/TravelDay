package com.example.travelday_2

import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.travelday_2.databinding.FragmentOutfitBinding
import java.text.SimpleDateFormat


class OutfitFragment : Fragment() {

    val PERM_STORAGE = 9
    val PERM_CAMERA = 10
    val PERM_GALLERY = 12

    val REQ_CAMERA = 11
    val REQ_GALLERY = 13
    lateinit var binding: FragmentOutfitBinding
    var realUri: Uri?=null

    private val PREF_IMAGE_URI = "pref_image_uri"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOutfitBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        requirePermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), PERM_STORAGE)
        initBackStack()

        val savedImageUri = getSavedImageUri()
        if(savedImageUri != null){
            loadImage(savedImageUri)
        }
    }
    private fun requirePermissions(permissions: Array<String>, requestCode: Int) {
        val context = requireContext()
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                missingPermissions.toTypedArray(),
                requestCode
            )
        } else {
            permissionGranted(requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            val deniedPermissions = mutableListOf<String>()
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i])
                }
            }
            if (deniedPermissions.isEmpty()) {
                permissionGranted(requestCode)
            } else {
                permissionDenied(requestCode)
            }
        }
    }

    fun initView(){
        binding.cameraBtn.setOnClickListener {
            requirePermissions(arrayOf(android.Manifest.permission.CAMERA), PERM_CAMERA)
        }

        binding.galleryBtn.setOnClickListener {
            requirePermissions(
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ), PERM_GALLERY
            )
        }
    }
    fun createImageUri(filename:String,mimeType:String): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME,filename)
        values.put(MediaStore.Images.Media.MIME_TYPE,mimeType)

        return requireContext().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    fun newfileName() : String{
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }

    fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, REQ_GALLERY)
    }

    fun openCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        createImageUri(newfileName(), "image/jpg")?.let{ uri->realUri = uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, realUri)
            startActivityForResult( intent, REQ_CAMERA)
        }

    }
    fun loadImage(imageUri: Uri){
        val bitmap = loadBitmap(imageUri)
        binding.imageView.setImageBitmap(bitmap)
    }
    fun loadBitmap(photoUri: Uri) : Bitmap?{
        try{
            return if(Build.VERSION.SDK_INT> Build.VERSION_CODES.O_MR1){
                val source = ImageDecoder.createSource(requireContext().contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            }else{
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver,photoUri)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return null
    }
    private fun permissionGranted(requestCode: Int) {
        when (requestCode) {
            PERM_STORAGE -> initView()
            PERM_CAMERA -> openCamera()
            PERM_GALLERY -> openGallery()
        }
    }
    private fun permissionDenied(requestCode: Int) {
        when (requestCode) {
            PERM_STORAGE -> {
                Toast.makeText(
                    requireContext(),
                    "공용 저장소 권한을 승인해야 앱을 사용할 수 있습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().finish()
            }
            PERM_CAMERA -> {
                Toast.makeText(
                    requireContext(),
                    "카메라 권한을 승인해야 카메라를 사용할 수 있습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            PERM_GALLERY -> {
                Toast.makeText(
                    requireContext(),
                    "갤러리 권한을 승인해야 카메라를 사용할 수 있습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            when(requestCode){
                REQ_CAMERA->{
                    realUri?.let{
                            uri->val bitmap = loadBitmap(uri)
                        binding.imageView.setImageBitmap(bitmap)
                        saveImageUri(uri)
                        realUri = null
                    }
                }
                REQ_GALLERY->{
                    data?.data.let { uri->
                        binding.imageView.setImageURI(uri)
                    }
                }
            }
        }
    }
    private fun initBackStack() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun getSavedImageUri(): Uri?{
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val imageUriString = sharedPreferences.getString(PREF_IMAGE_URI, null)
        return imageUriString?.let{Uri.parse(it)}
    }

    private fun saveImageUri(uri: Uri){
        val sharePreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val editor = sharePreferences.edit()
        editor.putString(PREF_IMAGE_URI,uri.toString())
        editor.apply()
    }

}