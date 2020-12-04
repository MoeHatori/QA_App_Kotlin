package jp.techacademy.moe.hatori.qa_app_kotlin

import java.io.Serializable

//質問の回答のためのモデルクラス
class Favorites(val uid: String, val genre: String ) : Serializable

//Uid:お気に入り登録
//genre:お気に入り登録されたコンテンツのジャンル