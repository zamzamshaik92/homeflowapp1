package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.model.FamilyMember
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY id ASC")
    fun getAllMembers(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE id = :id LIMIT 1")
    suspend fun getMemberById(id: Long): FamilyMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<FamilyMember>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: FamilyMember): Long

    @Update
    suspend fun update(member: FamilyMember)

    @Delete
    suspend fun delete(member: FamilyMember)

    @Query("DELETE FROM family_members WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE family_members SET allowanceBalance = allowanceBalance + :amount WHERE id = :memberId")
    suspend fun addAllowance(memberId: Long, amount: Double)

    @Query("UPDATE family_members SET allowanceBalance = allowanceBalance - :amount WHERE id = :memberId")
    suspend fun deductAllowance(memberId: Long, amount: Double)

    @Query("SELECT COUNT(*) FROM family_members")
    suspend fun getCount(): Int

    @Query("DELETE FROM family_members")
    suspend fun clearAll()
}
