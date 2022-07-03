package com.amine.myanimeapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_character.*
import java.io.ByteArrayOutputStream


class CharacterFragment : Fragment() {

    var choosesImage : Uri? = null
    var chooseBitmap : Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_character, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener {
            save(it)
        }
        imageView2.setOnClickListener {
            chooseimage(it)
        }
        arguments?.let {
            var cameInformation = CharacterFragmentArgs.fromBundle(it).information
            if(cameInformation.equals("camefrommenu")){
                // YENİ ANİME EKLENECEK
                animenameText.setText("")
                animecharacterText.setText("")
                button.visibility=View.VISIBLE  // button görünür olsun

                val visualSelectBackground = BitmapFactory.decodeResource(context?.resources,R.drawable.image) //!!!!!!!!!!!!!
                imageView2.setImageBitmap(visualSelectBackground)
            } else{
                // DAHA ÖNCE OLUŞTURULAN ANİME GÖRMEYE GELDİ
                button.visibility=View.INVISIBLE  // buton görünürlüğü kapattık

                val chooseId = CharacterFragmentArgs.fromBundle(it).id

                context?.let {
                    try{
                        val database = it.openOrCreateDatabase("Animes",Context.MODE_PRIVATE,null)
                        val cursor = database.rawQuery("SELECT * FROM animes WHERE id=? ", arrayOf(chooseId.toString()))

                        val  animenameIndex = cursor.getColumnIndex("animename")
                        val animecharacterIndex = cursor.getColumnIndex("animecharacter")
                        val animeImages = cursor.getColumnIndex("image")

                        while(cursor.moveToNext()){
                            animenameText.setText(cursor.getString(animenameIndex))
                            animecharacterText.setText(cursor.getString(animecharacterIndex))

                            // BUNUNLA BİRLİKTE ALDIĞIMIZ BYTE DİZİSİNİ TEKRAR BİTMAP ÇEVİRİYORUZ
                            val byteArray = cursor.getBlob(animeImages)
                            val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                            imageView2.setImageBitmap(bitmap)

                        }
                        cursor.close()

                    } catch(e : Exception) {
                        e.printStackTrace()

                    }
                }

            }
        }
    }

    fun save(view: View){
        // SQLite kaydetme
        val animeName = animenameText.text.toString()
        val animecharacter = animecharacterText.text.toString()

        if(chooseBitmap != null){
            val littleBitmap = littleBimapCreate(chooseBitmap!!,300)

            // VERİ TABANINA VERİ KAYDETMEK İÇİN , GÖRSELİ VERİ YE DÖNÜŞTÜRMEMİZ GEREK
            // BİTMAP VERİYE ÇEVİRMEK İÇİN GEREKLİ OLAN KODLAR
            val outputStream= ByteArrayOutputStream()
            littleBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            // SQLite KAYDETME
            try{
                context?.let {
                    val database = it.openOrCreateDatabase("Animes", Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS animes (id INTEGER PRIMARY KEY, animename VARCHAR, animecharacter VARCHAR, image BLOB)")

                    val sqlString = "INSERT INTO animes (animename,animecharacter,image) VALUES (?,?,?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1,animeName)
                    statement.bindString(2,animecharacter)
                    statement.bindBlob(3,byteArray)
                    statement.execute()
                }

            }catch(e : Exception){
                e.printStackTrace()
            }

            val action = CharacterFragmentDirections.actionCharacterFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)

        }

    }

    fun chooseimage(view:View){
        /*Mesela kullanıcıdan galeriye erişmek için izin alırken,
            telefonun API 19  önce ya da sonrası diye düşünmeden izni çalıştırabilicez
                onun için ContextCompat kullandık */
        // checkSelfPermission : ile de izni kontrol ediyoruz.

        activity?.let {
            // aktivite varsa var, yoksa yok anlamında
            if(ContextCompat.checkSelfPermission(it.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                // izin verildiye eşit değilse
                // YANİ , izin verilmedi, izin verilmesi gerekiyor
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
                // izni istedim

            } else {
                // izin zaten verilmiş, izin istemeden galeriye git
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent,2)
                // geriye birşey döndürücez
            }
        }
    }

    override fun onRequestPermissionsResult(
        // İSTENİLEN İZİNLERİN SOUÇLARI DEĞERLENDİRİLİYOR
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==1){
            if(grantResults.size>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Geriye birşey döndü mü VEsonuçlar izin verildiyse
                // İZNİ ALDIK
                // İZNİ ALINCA GALERİYE GİDİCEM
                val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent,2) // bir sonuç için aktiviteyi başlat demek

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode==2 && resultCode == Activity.RESULT_OK && data != null){

            choosesImage = data.data // SEÇİLEN GÖRSELİN NERDE DURDUĞUNU ALMIŞ OLDUM
            // KONUMUNU ALDIMM
            // URI

            try{
                context?.let {
                    if(choosesImage != null){
                        if(Build.VERSION.SDK_INT >=28){
                            // TELEFONU  SDK 28 VE ÜSTÜ OLANLAR İÇİN KONTROL
                            // creayeSource telefonda sadece SDK  28 ve üstünde çalışıyor
                            var source = ImageDecoder.createSource(it.contentResolver,choosesImage!!)
                            chooseBitmap = ImageDecoder.decodeBitmap(source)
                            imageView2.setImageBitmap(chooseBitmap)
                        } else {
                            // TELEFON SDK 28 DEN KÜÇÜKSE
                            chooseBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,choosesImage)
                            imageView2.setImageBitmap(chooseBitmap)
                        }
                    }
                }
            } catch(e : Exception){
                e.printStackTrace() // sorunları yazdırıyor
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun littleBimapCreate(chooseUserBitmap : Bitmap,maxSize : Int) : Bitmap{

        var width = chooseUserBitmap.width
        var height = chooseUserBitmap.height
        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if(bitmapRatio>1){
            // görsel yatay
            width= maxSize
            val abbreviatedHeight = width/bitmapRatio
            height=abbreviatedHeight.toInt()

        }else{
            // görsel dikey
            height= maxSize
            val abbreviatedWidth = height * bitmapRatio
            width= abbreviatedWidth.toInt()
        }
        return Bitmap.createScaledBitmap(chooseUserBitmap,width,height,true)
    }
}