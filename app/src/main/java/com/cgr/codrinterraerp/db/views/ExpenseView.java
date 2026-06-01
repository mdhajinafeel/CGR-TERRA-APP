package com.cgr.codrinterraerp.db.views;

import androidx.room.DatabaseView;

import java.io.Serializable;

@DatabaseView(
        viewName = "expense_view",
        value = "SELECT e.transactionId, e.tempTransactionId, e.transactionDisplayId, e.beneficiaryName, e.beneficiaryIdentification, e.expenseDate, " +
                "IFNULL(e.amount,0) as amount, a.accountHeadName, a.icon, a.isForestry, a.accountHeadId, e.attachFileUri, e.capturedTimeStamp " +
                "FROM expense_data e " +
                "INNER JOIN account_heads a ON a.accountHeadId = e.accountHeadId " +
                "WHERE e.isDeleted = 0"
)
public class ExpenseView implements Serializable {

    public int transactionId, accountHeadId;
    public String tempTransactionId, transactionDisplayId, beneficiaryName, beneficiaryIdentification, expenseDate, accountHeadName, icon, attachFileUri;
    public double amount;
    public boolean isForestry;
    public long capturedTimeStamp;
}
