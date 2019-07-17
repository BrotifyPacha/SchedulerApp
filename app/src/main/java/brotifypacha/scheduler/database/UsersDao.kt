package brotifypacha.scheduler.database

import androidx.room.*
import brotifypacha.scheduler.data_models.UserModel

@Dao
abstract class UsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(user: User)

    @Ignore
    fun insertModel(users: List<UserModel>){
        users.forEach {
            insert(User(it._id, it.username))
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(user: List<User>)

    @Update
    abstract fun update(user: User)

    @Delete
    abstract fun delete(user: User)

    @Ignore
    fun findByUsername(username: String): List<User>{
        return _findByUsername("%${username}%")
    }
    @Query("SELECT * FROM users_table WHERE username LIKE :username")
    abstract fun _findByUsername(username: String): List<User>
}