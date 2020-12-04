package jp.techacademy.moe.hatori.qa_app_kotlin

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar  // ← 追加
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
// findViewById()を呼び出さずに該当Viewを取得するために必要となるインポート宣言
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.app_bar_main.fab
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_main.listView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mToolbar: Toolbar? = null
    private var mGenre = 0

    private lateinit var mDatabaseReference: DatabaseReference
     //Firebaseに書き込むために必要なクラス
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter
     //質問画面一覧のために必要なQuestionクラスのリストとアダプタ

    private var mGenreRef: DatabaseReference? = null

    private var favoriteList = ArrayList<Favorites>()

    //questionListAdapterへのデータの設定
    private val mEventListener = object : ChildEventListener {

        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            val map = dataSnapshot.value as Map<String, String>
            //Log.d("Test1",map.toString())
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = map["image"] ?: ""
            val bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String, String>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }

            val question = Question(title, body, name, uid, dataSnapshot.key ?: "",
                mGenre, bytes, answerArrayList)
            //Log.d("Test2",question.toString())
            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            // 変更があったQuestionを探す
            for (question in mQuestionArrayList) {
                if (dataSnapshot.key.equals(question.questionUid)) {
                    // このアプリで変更がある可能性があるのは回答（Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }

                    mAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // idがtoolbarがインポート宣言により取得されているので
        // id名でActionBarのサポートを依頼
        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)

        Log.d("Test","OnCreateされました")


        // fabにClickリスナーを登録
        fab.setOnClickListener { view ->
            // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
            if (mGenre == 0) {
                Snackbar.make(view, getString(R.string.question_no_select_genre), Snackbar.LENGTH_LONG).show()
            }

            Log.d("Test","クリックされました")
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser
            Log.d("Test",user.toString())
            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // ジャンルを渡して質問作成画面を起動する
                val intent = Intent(applicationContext, QuestionSendActivity::class.java)
                intent.putExtra("genre", mGenre)
                startActivity(intent)
            }
        }

        // ナビゲーションドロワーの設定
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout) //activity_main.xml
        val toggle = ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        //toggle?.isDrawerIndicatorEnabled = false



        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        listView.setOnItemClickListener{parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }


    }

    override fun onResume() {
        super.onResume()
        // 1:趣味を既定の選択とする
        if(mGenre == 0) {
            onNavigationItemSelected(nav_view.menu.getItem(0))
        }

        //★ログインしているときはドロワーに「お気に入り」を表示させる処理
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.getMenu().clear()
        val user_login = FirebaseAuth.getInstance().currentUser
        if ( user_login != null){
            navigationView.inflateMenu(R.menu.activity_main_drawer)
            navigationView.inflateMenu(R.menu.activity_main_logindrawer)
        }else{
            navigationView.inflateMenu(R.menu.activity_main_drawer)
        }
        navigationView.setNavigationItemSelectedListener(this)
        Log.d("Test_OnResume",user_login.toString())

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val user = FirebaseAuth.getInstance().currentUser

        if (id == R.id.nav_hobby) {
            toolbar.title = getString(R.string.menu_hobby_label)
            mGenre = 1
        } else if (id == R.id.nav_life) {
            toolbar.title = getString(R.string.menu_life_label)
            mGenre = 2
        } else if (id == R.id.nav_health) {
            toolbar.title = getString(R.string.menu_health_label)
            mGenre = 3
        } else if (id == R.id.nav_compter) {
            toolbar.title = getString(R.string.menu_compter_label)
            mGenre = 4
        } else if (id == R.id.nav_favorite){
            toolbar.title = getString(R.string.menu_favorite_label)
            mGenre = 5
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        listView.adapter = mAdapter

        // 選択したジャンルにリスナーを登録する
        if (mGenreRef != null) {
            mGenreRef!!.removeEventListener(mEventListener)
        }
        //★お気に入りが選択されたらmGenreRefを変更する
        if ( mGenre != 5){
            mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
            mGenreRef!!.addChildEventListener(mEventListener)
        } else {
            if(user != null){
                //★favoritesを読み込んでリストを作成する
                val favoriteRef = mDatabaseReference.child(FavoritePATH).child(user.uid)
                val favoriteListRef = mDatabaseReference.child(ContentsPATH)

                favoriteRef.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //★読み込んできたログインUserのfavoriteをリストで保持する
                        val favoriteResult = snapshot.value as Map<String, String>?

                        if (favoriteResult != null){
                            for (key in favoriteResult.keys){
                                val temp = favoriteResult[key] as Map<String, String>
                                val favoriteGenre = temp["genre"] ?: ""
                                val favorite = Favorites(key,favoriteGenre)
                                Log.d("Test_FavList",favorite.uid + favorite.genre)
                                //favoriteList.add(favorite)
                                favoriteListRef.child(favoriteGenre).child(key).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        val map = snapshot.value as Map<String, String>
                                        //Log.d("Test1",map.toString())
                                        val title = map["title"] ?: ""
                                        val body = map["body"] ?: ""
                                        val name = map["name"] ?: ""
                                        val uid = map["uid"] ?: ""
                                        val imageString = map["image"] ?: ""
                                        val bytes =
                                            if (imageString.isNotEmpty()) {
                                                Base64.decode(imageString, Base64.DEFAULT)
                                            } else {
                                                byteArrayOf()
                                            }

                                        val answerArrayList = ArrayList<Answer>()
                                        val answerMap = map["answers"] as Map<String, String>?
                                        if (answerMap != null) {
                                            for (key in answerMap.keys) {
                                                val temp = answerMap[key] as Map<String, String>
                                                val answerBody = temp["body"] ?: ""
                                                val answerName = temp["name"] ?: ""
                                                val answerUid = temp["uid"] ?: ""
                                                val answer = Answer(answerBody, answerName, answerUid, key)
                                                answerArrayList.add(answer)
                                            }
                                        }

                                        val question = Question(title, body, name, uid, snapshot.key ?: "",
                                            favoriteGenre.toInt(), bytes, answerArrayList)

                                        mQuestionArrayList.add(question)
                                        mAdapter.notifyDataSetChanged()
                                    }
                                    override fun onCancelled(firebaseError: DatabaseError) {}
                                })
                            }
                        }

                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

                //mGenreRef = mDatabaseReference.child(FavoritePATH).child(user.uid)
            }
        }


        return true
    }

}