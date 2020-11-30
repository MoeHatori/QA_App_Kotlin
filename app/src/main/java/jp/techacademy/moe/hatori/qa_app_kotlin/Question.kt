package jp.techacademy.moe.hatori.qa_app_kotlin

import java.io.Serializable
import java.util.ArrayList

//取得した質問のモデルクラスであるAnswerのArrayList
class Question(val title: String, val body: String, val name: String, val uid: String, val questionUid: String, val genre: Int, bytes: ByteArray, val answers: ArrayList<Answer>) : Serializable {
    val imageBytes: ByteArray

    init {
        imageBytes = bytes.clone()
    }
}


//title:取得したタイトル
//body:取得した質問本文
//name:質問者の名前
//uid:質問者のUID
//questionUid:質問のUID
//genre:質問のジャンル
//imageBytes:画像をbyte型の配列にしたもの
//answers:取得した質問のモデルクラスであるAnswerのArrayList