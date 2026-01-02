UPDATE dbo.Expense
SET CategoryID = CASE
    WHEN Category = 'HEALTH' THEN (SELECT TOP 1 ID FROM dbo.ExpenseCategory WHERE Name = 'Health')
    WHEN Category = 'CLOTHING' THEN (SELECT TOP 1 ID FROM dbo.ExpenseCategory WHERE Name = 'Clothing')
    WHEN Category = 'UTILITIES' THEN (SELECT TOP 1 ID FROM dbo.ExpenseCategory WHERE Name = 'Utilities')
    WHEN Category = 'ELECTRONICS' THEN (SELECT TOP 1 ID FROM dbo.ExpenseCategory WHERE Name = 'Electronics')
    WHEN Category = 'LEISURE' THEN (SELECT TOP 1 ID FROM dbo.ExpenseCategory WHERE Name = 'Leisure')
    WHEN Category = 'GROCERIES' THEN (SELECT TOP 1 ID FROM dbo.ExpenseCategory WHERE Name = 'Groceries')
    ELSE (SELECT TOP 1 ID FROM dbo.ExpenseCategory WHERE Name = 'Others')
END