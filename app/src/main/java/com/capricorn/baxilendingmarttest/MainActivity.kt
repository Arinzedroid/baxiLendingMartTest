package com.capricorn.baxilendingmarttest

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.capricorn.baxilendingmarttest.extension.hasWriteToStoragePermission
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.lang.Exception
import java.util.*


private const val USERNAME = "baxi.lendingmart.username"
private const val AGENTID = "baxi.lendingmart.agentid"
private const val PLATFORM = "baxi.lendingmart.platform"
class MainActivity : AppCompatActivity() {

    private var downloaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PRDownloader.initialize(this.applicationContext)

        //checkAppInstalled()

    }

    override fun onResume() {
        super.onResume()
        if(downloaded)
            checkapp()
        else
            checkAppInstalled()
    }

    private fun getProgressDisplayLine(currentBytes: Long, totalBytes: Long): String? {
        return getBytesToMBString(currentBytes)
        //+ "/" + getBytesToMBString(totalBytes)
    }

    private fun getBytesToMBString(bytes: Long): String {
        return String.format(Locale.ENGLISH, "Downloading... %.2fMB", bytes / (1024.00 * 1024.00))
    }

    private fun calcPercent(total: Long, current: Long){
        //val percent = (current * 100 / total)

        //progress_drawable?.progress = percent.toInt()
        //println("PERCENT: ${percent.toInt()} \t CURRENT: $current \t total: $total")
        desc_tv?.text = getProgressDisplayLine(current,total)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(grantResults.isNotEmpty()){
            var ok = true
            grantResults.forEach {
                ok = ok && (it == PackageManager.PERMISSION_GRANTED)
            }

            when(requestCode){
                20 -> {
                    if(ok){
                        downloadFile()
                    }else{
                        val msg = "Please accept permissions to enable us download and save config files"
                        showPopUp(this, "Permission Required",msg,"Continue",
                        "Cancel",::checkAppInstalled, ::close)
                    }
                }
            }
        }

    }

    private fun close(){
        finish()
    }

    private fun startAPK(){
        try{
            val intent = Intent("com.capricorn.baxilendingmart.HomeActivity")
            intent.putExtra(USERNAME, "dobdaniel")
            intent.putExtra(AGENTID,"012590")
            intent.putExtra(PLATFORM,"app")

            startActivity(intent)
            finish()
        }catch (ex: Exception){
            ex.printStackTrace()
            Toast.makeText(this, "Error initializing app", Toast.LENGTH_SHORT).show()
        }

    }

    private fun checkapp(){
        val packageName = "com.capricorn.baxilendingmart"
        if(isAppInstalled(this, packageName)){
            //start application class
            startAPK()
        }
    }

    private fun checkAppInstalled(){
        val packageName = "com.capricorn.baxilendingmart"
        if(isAppInstalled(this, packageName)){
            //start application class
            startAPK()
        }
        else{
            //start downloading apk
            if(hasWriteToStoragePermission() == PackageManager.PERMISSION_GRANTED)
                if(!downloaded)
                    downloadFile()
            else
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 20)
        }
    }

    private fun isAppInstalled(context: Context, uri: String): Boolean {
        val pm: PackageManager = context.packageManager
        val installed: Boolean
        installed = try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
        return installed
    }

    private fun startInstall(file: File?, filename: String){
        val intent = Intent(Intent.ACTION_VIEW)
        file?.also {
            val uri = FileProvider.getUriForFile(this,
                    "com.capricorn.baxilendingmarttest.provider", File(it,filename))
            println("URI: $uri")
            println("FILE: $file")
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        }

    }

    private fun downloadFile(){
        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.also {
            val url = "https://drive.google.com/uc?id=1GPf0u6cJNwr1JV6gn5j8199V-K3EvLh-&export=download"
            val int = PRDownloader.download(url,it.path, "baxi_lending.apk")
                    .build()
                    .setOnProgressListener { t ->
                        calcPercent(t.totalBytes, t.currentBytes)
                    }
                    .start(object: OnDownloadListener{
                        override fun onDownloadComplete() {
                            downloaded = true
                            desc_tv?.text = "Download completed"

                            Toast.makeText(this@MainActivity, "Download completed", Toast.LENGTH_SHORT).show()
                            startInstall(it,"baxi_lending.apk")
                        }

                        override fun onError(error: Error?) {
                            println("IsServerError: ${error?.isServerError}")
                            println("IsConnectError: ${error?.isConnectionError}")
                            println("HeaderFields: ${error?.headerFields}")
                            error?.connectionException?.printStackTrace()
                            println("ServerErrorMsg: ${error?.serverErrorMessage}")
                            desc_tv?.text = "Download error!"
                            Toast.makeText(this@MainActivity, "Error on downloading", Toast.LENGTH_SHORT).show()

//                            //comment out
//                            downloaded = true
//                            startInstall(it,"baxi_lending.apk")
                        }
                    })
        }

    }

    private fun showPopUp(context: Context,title: String, desc: String?, positive: String,
                  negative: String, pAction:() -> Unit, nAction:() -> Unit){
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setTitle(title)
        builder.setMessage(desc)
        builder.setPositiveButton(positive
        ) { dialog, _ -> dialog.dismiss()
            pAction()
        }
        builder.setNegativeButton(negative
        ) { dialog, _ ->
            nAction()
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    /*private fun getDir(): File{
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Date())
        val dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        File.createTempFile("baxi_${timeStamp}_",".apk", dir).apply {
            return this
        }
    }

    private fun createFile(){
        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.also {
            val file = File(it, "baxi_lending.apk")
            if(!file.exists()){
                val b = file.createNewFile()
                println("File created: $b direction: ${file.path}")
            }
            startInstall(file)
        }
    }

    private fun startInstall(file: File?){
        val intent = Intent(Intent.ACTION_VIEW)
        file?.also {
            val uri = FileProvider.getUriForFile(this,
                    "com.capricorn.baxilendingmarttest.provider",it)
            println("URI: $uri")
            println("FILE: $file")
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, false)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        }
    }

    private fun startDownload(drive: Drive?){
        val fileid = "19LXbxy2o3p94Gxd8E2UqLVvSzK1nhW4O"
        drive?.files()?.get(fileid)?.executeMediaAndDownloadTo(ByteArrayOutputStream())
    }*/
}