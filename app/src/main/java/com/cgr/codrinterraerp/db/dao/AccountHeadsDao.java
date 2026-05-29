package com.cgr.codrinterraerp.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cgr.codrinterraerp.db.entities.AccountHeads;

import java.util.List;

@Dao
public interface AccountHeadsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAccountHeads(List<AccountHeads> accountHeads);

    @Query("SELECT * FROM account_heads ORDER BY accountHeadId ASC")
    List<AccountHeads> getAllAccountHeads();

    @Query("DELETE FROM account_heads")
    void clearAll();
}