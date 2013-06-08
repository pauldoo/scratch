%mortgageSize = 203460;
%yearlyInterestRate = 0.0234;
%mortgageSize = 203460;
%yearlyInterestRate = 0.04;
mortgageSize = 255200;
yearlyInterestRate = 0.0254;
termInYears = 30;

solvePayments(mortgageSize, yearlyInterestRate, 0.00, termInYears);
solvePayments(mortgageSize, yearlyInterestRate, 0.01, termInYears);
solvePayments(mortgageSize, yearlyInterestRate, 0.02, termInYears);
solvePayments(mortgageSize, yearlyInterestRate, 0.03, termInYears);

