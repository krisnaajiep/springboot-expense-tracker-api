-- Drop the 'Category' column from the 'Expense' table along with its associated constraint.

ALTER TABLE dbo.Expense
DROP CONSTRAINT CK_Expense_Category;

ALTER TABLE dbo.Expense
DROP COLUMN Category;