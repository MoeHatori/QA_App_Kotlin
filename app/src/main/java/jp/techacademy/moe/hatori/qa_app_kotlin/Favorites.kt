package jp.techacademy.moe.hatori.qa_app_kotlin

import java.io.Serializable

//質問の回答のためのモデルクラス
class Favorites(val uid: String, val title: String) : Serializable

//Uid:お気に入り登録
//title:お気に入り登録されたコンテンツのタイトル