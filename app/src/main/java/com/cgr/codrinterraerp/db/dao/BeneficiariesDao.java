package com.cgr.codrinterraerp.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.cgr.codrinterraerp.db.entities.Beneficiaries;

import java.util.List;

@Dao
public interface BeneficiariesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBeneficiaries(List<Beneficiaries> beneficiaries);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBeneficiary(Beneficiaries beneficiary);

    @Query(" SELECT * FROM beneficiaries GROUP BY beneficiaryName, beneficiaryIdentification")
    List<Beneficiaries> getAllBeneficiaries();

    @Query("DELETE FROM beneficiaries")
    void clearAll();
}