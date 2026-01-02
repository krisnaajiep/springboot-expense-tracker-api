CREATE TABLE dbo.UserAccount
(
    ID        bigint IDENTITY (1,1) PRIMARY KEY NOT NULL,
    CreatedAt datetime2(6)                      NOT NULL,
    UpdatedAt datetime2(6)                      NOT NULL,
    Email     varchar(255) UNIQUE               NOT NULL,
    Name      varchar(255)                      NOT NULL,
    Password  varchar(255)                      NOT NULL
);

CREATE TABLE dbo.RefreshToken
(
    ID         bigint IDENTITY (1,1) PRIMARY KEY NOT NULL,
    CreatedAt  datetime2(6)                      NOT NULL,
    UpdatedAt  datetime2(6)                      NOT NULL,
    ExpiryDate datetimeoffset(6)                 NOT NULL,
    RotatedAt  datetimeoffset(6),
    Token      varchar(255) UNIQUE               NOT NULL,
    UserID     bigint                            NOT NULL,
    CONSTRAINT FK_RefreshToken_UserAccount FOREIGN KEY (UserID) REFERENCES dbo.UserAccount (ID)
)

CREATE TABLE dbo.Expense
(
    ID          uniqueidentifier PRIMARY KEY NOT NULL,
    CreatedAt   datetime2(6)                 NOT NULL,
    UpdatedAt   datetime2(6)                 NOT NULL,
    Amount      numeric(38, 2)               NOT NULL,
    Category    varchar(20)                  NOT NULL,
    [Date]      date                         NOT NULL,
    Description varchar(255)                 NOT NULL,
    UserID      bigint                       NOT NULL,
    CONSTRAINT CK_Expense_Category CHECK (Category IN ('OTHERS', 'HEALTH', 'CLOTHING', 'UTILITIES', 'ELECTRONICS', 'LEISURE', 'GROCERIES')),
    CONSTRAINT FK_Expense_UserAccount FOREIGN KEY (UserID) REFERENCES dbo.UserAccount (ID)
)



