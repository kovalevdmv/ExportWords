package ru.kdv.exportwords
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    fun onClick1(view: View) {
        if (ContextCompat.checkSelfPermission(this@MainActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) !==
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            } else {
                ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }
        }
        if (ContextCompat.checkSelfPermission(this@MainActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) !==
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            } else {
                ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }

        try {
            var ExDir = externalMediaDirs[0].toString()
            var fn = ExDir + File.separator + "Words.txt"
            var fn_db = ExDir + File.separator + "WordsDB.db3"
            try {
                var db = SQLiteDatabase.openDatabase(
                        fn_db,
                        null,
                        SQLiteDatabase.OPEN_READWRITE
                )

                var textQ = """
                    select 
                    ifnull(B.question_ru,"") as question_ru,
                    ifnull(B.question_en,"") as question_en,
                    ifnull(B.sentence_ru,"") as sentence_ru,
                    ifnull(B.sentence_en,"") as sentence_en,
                    ifnull(transcription,"") as trans
                    from WordsTable as T
                    left join BaseWordsTable as B
                    on T.id_word = B.id_word
                    where T.is_known = 0 and B.id_word is not null
                    order by date_learned
                        """;

                if(checkBox.isChecked){
                    textQ = textQ.replace("T.is_known = 0 and","");
                }

                var c = db.rawQuery(textQ, null)
                Toast.makeText(this,  c.getCount().toString() + " words", Toast.LENGTH_LONG).show()
                Log.d("db", c.getCount().toString())
                var str=""
                while(c.moveToNext()) {
                    str += c.getString(c.getColumnIndex("question_ru")) + ";" + c.getString(c.getColumnIndex("question_en")) + ";" + c.getString(c.getColumnIndex("sentence_ru")) + ";" + c.getString(c.getColumnIndex("sentence_en"))+ ";" + c.getString(c.getColumnIndex("trans")) + "\n"
                }
                var f = File(fn)
                f.writeText(str)
                c.close()
                db.close()

            } catch (e: Exception){
                Log.d("db", e.toString())
            }
        } catch (e: Exception){
            Log.e("1", e.toString())
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    fun onClickFromSmartBook(view: View) {
        var ExDir = externalMediaDirs[0].toString()
        var dic = ExDir + File.separator + "smart-book.txt"
        var Source = File(dic).readText()
        var rg = ".*\t.*".toRegex()
        var newText = ""
        for (i in rg.findAll(Source)){
            var arr = i.groups[0]?.value?.split("\t")
            newText += "${arr!![1]};${arr!![0]}\n"
        }
        if (!newText.isEmpty()) {
            File(dic).writeText(newText)
            Toast.makeText(this, "Ready", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Not find text in file", Toast.LENGTH_SHORT).show()
        }


    }
}
