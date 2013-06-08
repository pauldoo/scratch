%mortgageSize = 203460;
%yearlyInterestRate = 0.0234;
mortgageSize = 255200;
yearlyInterestRate = 0.0254;
termInYears = 30;

printf("Payments with 0%% inflation: %.2f\n", fminunc(@(p) (computeBalance(mortgageSize, yearlyInterestRate, p, 0.00, termInYears)^2), 0));
printf("Payments with 1%% inflation: %.2f\n", fminunc(@(p) (computeBalance(mortgageSize, yearlyInterestRate, p, 0.01, termInYears)^2), 0));
printf("Payments with 2%% inflation: %.2f\n", fminunc(@(p) (computeBalance(mortgageSize, yearlyInterestRate, p, 0.02, termInYears)^2), 0));
printf("Payments with 3%% inflation: %.2f\n", fminunc(@(p) (computeBalance(mortgageSize, yearlyInterestRate, p, 0.03, termInYears)^2), 0));

