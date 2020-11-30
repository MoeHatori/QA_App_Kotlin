package jp.techacademy.moe.hatori.qa_app_kotlin

import java.io.Serializable

//質問の回答のためのモデルクラス
class Answer(val body: String, val name: String, val uid: String, val answerUid: String) : Serializable

//body:Firebaseから取得した回答本文
//name:回答者の名前
//uid:回答者のUID
//answerUid:回答のUID