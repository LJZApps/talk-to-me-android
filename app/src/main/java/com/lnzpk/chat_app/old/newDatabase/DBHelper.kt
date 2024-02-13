package com.lnzpk.chat_app.old.newDatabase

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {
    val LOG_TAG = "Talk to me - Database-Log"

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // below is a sqlite query, where column names
        // along with their data types is given
        //val query = ("CREATE TABLE " + TABLE_NAME + " (" + ID_COL + " INTEGER PRIMARY KEY, " + NAME_COl + " TEXT," + AGE_COL + " TEXT" + ")")

        // we are calling sqlite
        // method for executing our query
        //db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        //db.execSQL("DROP TABLE IF EXISTS $CHAT_TABLE_NAME")

        //db.execSQL("CREATE TABLE 'test' ('chatName' INTEGER PRIMARY KEY)")

        /*
        val testName = "test";

        val CREATE_TABLE_DEPT = "CREATE TABLE $testName ($colidD INTEGER PRIMARY KEY, $colDept TEXT);"
        val CREATE_TABLE_ITEM = "CREATE TABLE $testName ($colidI INTEGER PRIMARY KEY, $colItem TEXT);"

        db!!.execSQL(CREATE_TABLE_DEPT)
        db!!.execSQL(CREATE_TABLE_ITEM)
         */

        onCreate(db)
    }

    fun isDataDownloaded(): Boolean{
        var boolCheck: Boolean = if(tableExists("chats")){
            if(tableExists("posts")){
                if(tableExists("groups")){
                    if(tableExists("friends")){
                        if(tableExists("app_settings")){
                            if(tableExists("profile_settings")){
                                if(tableExists("app_colors_dark") and tableExists("app_colors_dark")){
                                    if(tableExists("profiles")){
                                        if(tableExists("notifications")){
                                            tableExists("requests")
                                        }else{
                                            false
                                        }
                                    }else{
                                        false
                                    }
                                }else{
                                    false
                                }
                            }else{
                                false
                            }
                        }else{
                            false
                        }
                    }else{
                        false
                    }
                }else{
                    false
                }
            }else{
                false
            }
        }else{
            false
        }

        return boolCheck
    }

    fun getCurrentUsername(): String{
        val db = this.readableDatabase
        var returnValue: String
        if(tableExists("profiles")){
            var query = "SELECT username FROM profiles WHERE logged_in='1'"
            val cursor: Cursor = db.rawQuery(query, null)
            returnValue = if(cursor.moveToFirst()){
                cursor.getString(0)
            }else{
                throw Exception("No users logged in")
            }

            /*
            if (cursor.moveToFirst()) {
                returnValue = cursor.getString(cursor.getColumnIndexOrThrow("color_value"))
            }else{
                returnValue = "cursor is not moved to first"
            }
             */
        }else{
            returnValue = throw Exception("No users in Database")
        }

        db.close()

        return returnValue
    }

    fun loginNewUser(username: String, password: String, info: String, name: String, loggedIn: Boolean){
        val db = this.writableDatabase

        val logVal = if(loggedIn){
            1
        }else{
            0
        }

        db.execSQL("INSERT INTO profiles (username, password, info, name, logged_in) VALUES ('$username', '$password', '$info', '$name', '$logVal')")

        db.close()
    }

    fun setEveryUserToLoggedOut(){
        val db = this.writableDatabase

        db.execSQL("UPDATE profiles SET logged_in='0' WHERE logged_in='1'")

        db.close()
    }

    fun changeLoggedInUser(username: String, loggedIn: Boolean){
        val db = this.writableDatabase

        val logVal = if(loggedIn){
            1
        }else{
            0
        }

        db.execSQL("UPDATE profiles SET logged_in='$logVal' WHERE username='$username'")

        db.close()
    }

    /**
     * Checks if someone is logged in
     * @return 'true' if someone is logged in, 'false' if no one is logged in
     */
    fun isSomeoneLoggedIn(): Boolean{
        val db = this.readableDatabase
        var returnValue = false
        if(tableExists("app_settings")){
            var query = "SELECT * FROM profiles WHERE logged_in='1'"
            val cursor: Cursor = db.rawQuery(query, null)
            returnValue = cursor.count > 0

            /*
            if (cursor.moveToFirst()) {
                returnValue = cursor.getString(cursor.getColumnIndexOrThrow("color_value"))
            }else{
                returnValue = "cursor is not moved to first"
            }
             */
        }else{
            false
        }

        db.close()

        return returnValue
    }

    fun changeLastLoggedInState(username: String, lastOnline: String){
        val db = this.writableDatabase

        db.execSQL("UPDATE profiles SET last_online='$lastOnline' WHERE username='$username'")

        db.close()
    }

    fun setToDeletedProfile(username: String){
        val db = this.writableDatabase

        db.execSQL("UPDATE profiles SET deleted='1' WHERE username='$username'")

        db.close()
    }

    fun changeProfilePassword(username: String, password: String){
        val db = this.writableDatabase

        db.execSQL("UPDATE profiles SET password='$password' WHERE username='$username'")

        db.close()
    }

    fun changeProfileName(username: String, name: String){
        val db = this.writableDatabase

        db.execSQL("UPDATE profiles SET name='$name' WHERE username='$username'")

        db.close()
    }

    fun getProfileName(username: String): String{
        val db = this.readableDatabase
        var returnValue: String
        if(tableExists("profiles")){
            var query = "SELECT name FROM profiles WHERE username='$username'"
            val cursor: Cursor = db.rawQuery(query, null)
            returnValue = if(cursor.moveToFirst()){
                cursor.getString(0)
            }else{
                "NAME"
            }

            db.close()

            /*
            if (cursor.moveToFirst()) {
                returnValue = cursor.getString(cursor.getColumnIndexOrThrow("color_value"))
            }else{
                returnValue = "cursor is not moved to first"
            }
             */
        }else{
            returnValue = "NAME"
            db.close()
        }

        db.close()

        return returnValue
    }

    fun checkForDeletedProfile(username: String): Boolean{
        val db = this.readableDatabase
        var returnValue: Boolean
        if(tableExists("profiles")){
            var query = "SELECT deleted FROM profiles WHERE username='$username'"
            val cursor: Cursor = db.rawQuery(query, null)
            returnValue = if(cursor.moveToFirst()){
                cursor.getInt(0) == 1
                //cursor.getString(0)
            }else{
                false
            }

            /*
            if (cursor.moveToFirst()) {
                returnValue = cursor.getString(cursor.getColumnIndexOrThrow("color_value"))
            }else{
                returnValue = "cursor is not moved to first"
            }
             */
        }else{
            returnValue = true
        }

        db.close()

        return returnValue
    }

    fun deleteProfile(username: String){
        val db = this.writableDatabase

        if(doesUserExist(username)){
            db.execSQL("DELETE FROM profiles WHERE username='$username'")
            db.close()
        }else{
            db.close()
        }
    }

    fun getLastOnline(username: String): String{
        val db = this.readableDatabase
        var returnValue: String
        if(tableExists("profiles")){
            var query = "SELECT last_online FROM profiles WHERE username='$username'"
            val cursor: Cursor = db.rawQuery(query, null)
            returnValue = if(cursor.moveToFirst()){
                cursor.getString(0)
            }else{
                "2000-01-01 00:00"
            }

            /*
            if (cursor.moveToFirst()) {
                returnValue = cursor.getString(cursor.getColumnIndexOrThrow("color_value"))
            }else{
                returnValue = "cursor is not moved to first"
            }
             */
        }else{
            returnValue = "2000-01-01 00:00"
        }

        db.close()

        return returnValue
    }

    private fun doesUserExist(username: String): Boolean{
        val db = this.readableDatabase
        var returnValue = false
        if(tableExists("profiles")){
            var query = "SELECT * FROM profiles WHERE username='$username'"
            val cursor: Cursor = db.rawQuery(query, null)
            returnValue = cursor.count > 0
            db.close()

            /*
            if (cursor.moveToFirst()) {
                returnValue = cursor.getString(cursor.getColumnIndexOrThrow("color_value"))
            }else{
                returnValue = "cursor is not moved to first"
            }
             */
        }else{
            false
            db.close()
        }

        return returnValue
    }

    fun createTable(tableName: String){
        val db = this.writableDatabase

        db.execSQL("CREATE TABLE '$tableName' ('chat_name' VARCHAR PRIMARY KEY)")

        db.close()
    }

    fun putSettingString(settingName: String, settingValue: String){
        val db = this.writableDatabase

        if(doesSettingExist(settingName)){
            db.execSQL("UPDATE app_settings SET setting_value='$settingValue' WHERE setting_name='$settingName'")
        }else{
            db.execSQL("INSERT INTO app_settings (setting_name, setting_value) VALUES ('$settingName', '$settingValue')")
        }

        db.close()
    }

    fun putSettingBoolean(settingName: String, settingValue: Boolean){
        //Log.e(LOG_TAG, "Called 'putSettingBoolean' with settingName: $settingName and settingValue: $settingValue")

        val db = this.writableDatabase

        val logVal = if(settingValue){
            1
        }else{
            0
        }

        if(doesSettingExist(settingName)){
            db.execSQL("UPDATE app_settings SET setting_value='$logVal' WHERE setting_name='$settingName'")

            db.close()
        }else{
            db.execSQL("INSERT INTO app_settings (setting_name, setting_value) VALUES ('$settingName', '$logVal')")

            db.close()
        }
    }

    fun putSettingInt(settingName: String, settingValue: Int){
        val db = this.writableDatabase

        if(doesSettingExist(settingName)){
            db.execSQL("UPDATE app_settings SET setting_value='$settingValue' WHERE setting_name='$settingName'")
            db.close()
        }else{
            db.execSQL("INSERT INTO app_settings (setting_name, setting_value) VALUES ('$settingName', '$settingValue')")
            db.close()
        }
    }

    fun putSettingLong(settingName: String, settingValue: Long){
        val db = this.writableDatabase

        if(doesSettingExist(settingName)){
            db.execSQL("UPDATE app_settings SET setting_value='$settingValue' WHERE setting_name='$settingName'")
            db.close()
        }else{
            db.execSQL("INSERT INTO app_settings (setting_name, setting_value) VALUES ('$settingName', '$settingValue')")
            db.close()
        }
    }

    private fun doesSettingExist(settingName: String): Boolean{
        val db = this.readableDatabase
        var returnValue = false
        if(tableExists("app_settings")){
            var query = "SELECT * FROM app_settings WHERE setting_name='$settingName'"
            val cursor: Cursor = db.rawQuery(query, null)
            returnValue = cursor.count > 0
        }else{
            false
        }

        return returnValue
    }

    fun getSettingString(settingName: String, defaultValue: String): String{
        val db = this.readableDatabase
        var returnValue: String
        if(tableExists("app_settings")){
            var query = "SELECT setting_value FROM app_settings WHERE setting_name='$settingName'"
            val cursor: Cursor = db.rawQuery(query, null)
            returnValue = if(cursor.moveToFirst()){
                cursor.getString(0)
            }else{
                defaultValue
            }

            db.close()

            /*
            if (cursor.moveToFirst()) {
                returnValue = cursor.getString(cursor.getColumnIndexOrThrow("color_value"))
            }else{
                returnValue = "cursor is not moved to first"
            }
             */
        }else{
            returnValue = defaultValue
            db.close()
        }

        db.close()

        return returnValue
    }

    fun getSettingBoolean(settingName: String, defaultValue: Boolean): Boolean{
        val db = this.readableDatabase

        var returnValue = defaultValue
        if(tableExists("app_settings")){
            var query = "SELECT setting_value FROM app_settings WHERE setting_name='$settingName'"
            val cursor: Cursor = db.rawQuery(query, null)
            returnValue = if(cursor.moveToFirst()){
                val logVal = cursor.getInt(0)

                db.close()

                //Log.e(LOG_TAG, "Boolean-Integer of settingName: '$settingName' is: $logVal")

                logVal == 1
            }else{
                db.close()
                defaultValue
            }
        }

        return returnValue
    }

    fun getSettingInt(settingName: String, defaultValue: Int): Int{
        val db = this.readableDatabase

        var returnValue = defaultValue
        if(tableExists("app_settings")){
            var query = "SELECT setting_value FROM app_settings WHERE setting_name='$settingName'"
            val cursor: Cursor = db.rawQuery(query, null)
            returnValue = if(cursor.moveToFirst()){
                db.close()
                cursor.getInt(0)
            }else{
                db.close()
                defaultValue
            }
        }

        return returnValue
    }

    fun getSettingLong(settingName: String, defaultValue: Long): Long{
        val db = this.readableDatabase

        var returnValue = defaultValue
        if(tableExists("app_setting")){
            var query = "SELECT setting_value FROM app_settings WHERE setting_name='$settingName'"
            val cursor: Cursor = db.rawQuery(query, null)
            returnValue = if(cursor.moveToFirst()){
                db.close()
                cursor.getLong(0)
            }else{
                db.close()
                defaultValue
            }
        }

        return returnValue
    }

    fun deleteSetting(settingName: String){
        val db = this.writableDatabase

        db.execSQL("DELETE FROM app_settings WHERE setting_name='$settingName'")

        db.close()
    }

    fun tableExists(table: String?): Boolean {
        val db = this.readableDatabase

        if (db == null || !db.isOpen || table == null) {
            return false
        }
        var count = 0
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='$table'",
            null
        )
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()

        return count > 0
    }

    fun resetColors() {
        val db = this.writableDatabase

        if (tableExists("app_colors_dark")) {
            db.execSQL("DROP TABLE 'app_colors_dark'")
        }

        if (tableExists("app_colors_light")) {
            db.execSQL("DROP TABLE 'app_colors_light'")
        }

        db.close()
    }


    fun saveColor(colorName: String, colorValue: String, colorTheme: String){
        val db = this.writableDatabase

        if(colorTheme == "dark"){
            // if colorName exists:
            if(colorExists(colorName, "dark")){
                db.execSQL("UPDATE 'app_colors_dark' SET color_value='$colorValue' WHERE color_name='$colorName'")
            }else{
                db.execSQL("INSERT INTO 'app_colors_dark' (color_name, color_value) VALUES ('$colorName', '$colorValue')")
            }
        }else if(colorTheme == "light"){
            if(colorExists(colorName, "light")){
                db.execSQL("UPDATE 'app_colors_dark' SET color_value='$colorValue' WHERE color_name='$colorName'")
            }else{
                db.execSQL("INSERT INTO 'app_colors_light' (color_name, color_value) VALUES ('$colorName', '$colorValue')")
            }
        }

        db.close()
    }

    private fun colorExists(colorName: String, colorTheme: String): Boolean {
        val db = this.readableDatabase
        var query: String? = null
        if(colorTheme == "dark"){
            query = "SELECT 'color_value' FROM 'app_colors_dark' WHERE color_name = '$colorName'"
        }else if(colorTheme == "light"){
            query = "SELECT 'color_value' FROM 'app_colors_light' WHERE color_name = '$colorName'"
        }
        val cursor: Cursor = db.rawQuery(query, null)
        return if(cursor.moveToFirst()){
            cursor.count >= 1
        }else{
            false
        }
        db.close()
    }

    fun getColor(colorName: String, colorTheme: String): String {
        val db = this.readableDatabase
        var returnValue: String
        if(colorExists(colorName, colorTheme)){
            var query: String ?= null
            if(colorTheme == "dark"){
                query = "SELECT * FROM 'app_colors_dark' WHERE color_name ='$colorName'"
            }else if(colorTheme == "light"){
                query = "SELECT * FROM 'app_colors_light' WHERE color_name ='$colorName'"
            }
            val cursor: Cursor = db.rawQuery(query, null)
            returnValue = if(cursor.moveToFirst()){
                cursor.getString(cursor.getColumnIndexOrThrow("color_value"))
            }else{
                "0"
            }
            db.close()

            /*
            if (cursor.moveToFirst()) {
                returnValue = cursor.getString(cursor.getColumnIndexOrThrow("color_value"))
            }else{
                returnValue = "cursor is not moved to first"
            }
             */
        }else{
            db.close()
            returnValue = "0"
        }
        return returnValue
    }

    fun createChatTable(chatName: String){
        val db = this.writableDatabase

        db.execSQL("CREATE TABLE $chatName ('chat_name' VARCHAR PRIMARY KEY)")

        //db.close()
    }

    fun checkChatTable(chatName: String): Boolean {
        val db = this.readableDatabase

        return db.rawQuery("SELECT COUNT(*) FROM 'chatWith_$chatName'", null).count > 0
    }

    companion object {
        // here we have defined variables for our database

        // below is variable for database name
        private const val DATABASE_NAME = "AppDatabase"

        // below is the variable for database version
        private const val DATABASE_VERSION = 1
    }
}