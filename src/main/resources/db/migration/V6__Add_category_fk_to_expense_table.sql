ALTER TABLE dbo.Expense
ALTER COLUMN CategoryID bigint NOT NULL;

ALTER TABLE dbo.Expense
ADD CONSTRAINT FK_Expense_Category FOREIGN KEY (CategoryID) REFERENCES dbo.ExpenseCategory (ID);