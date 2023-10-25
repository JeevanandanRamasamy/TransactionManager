package banking;

import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

/**
 * Processes user input and runs program
 * @author Jeeva Ramasamy, Parth Patel
 */
public class TransactionManager {

    private static final String OPEN = "O";
    private static final String CLOSE = "C";
    private static final String DEPOSIT = "D";
    private static final String WITHDRAW = "W";
    private static final String PRINT = "P";
    private static final String PRINT_INTEREST_AND_FEES = "PI";
    private static final String UPDATE_BALANCE = "UB";
    private static final String QUIT = "Q";

    private static final double EMPTY = 0.0;
    private static final int MIN_AGE = 16;
    private static final int COLLEGE_MAX_AGE = 24;
    private static final int MIN_MONEYMARKET_DEPOSIT = 2000;

    /**
     * Runs the program
     */
    public void run() {
        System.out.println("Transaction Manager running.\n");
        AccountDatabase acctDb = new AccountDatabase();
        Scanner input = new Scanner(System.in);
        boolean isRunning = true;
        while (input.hasNextLine() && isRunning) {
            String line = input.nextLine();
            while (line.isEmpty())
                line = input.nextLine();
            StringTokenizer st = new StringTokenizer(line);
            String command = st.nextToken();
            switch (command) {
                case OPEN:
                    openAccount(acctDb, st);
                    break;
                case CLOSE:
                    closeAccount(acctDb, st);
                    break;
                case DEPOSIT:
                    deposit(acctDb, st);
                    break;
                case WITHDRAW:
                    withdraw(acctDb, st);
                    break;
                case PRINT:
                    acctDb.printSorted();
                    break;
                case PRINT_INTEREST_AND_FEES:
                    acctDb.printFeesAndInterests();
                    break;
                case UPDATE_BALANCE:
                    acctDb.printUpdatedBalances();
                    break;
                case QUIT:
                    isRunning = false;
                    System.out.println("Transaction Manager is terminated.");
                    break;
                default:
                    System.out.println("Invalid command!");
            }
        }
    }

    /**
     * Opens an account using the specified input
     * @param acctDb  the account database
     * @param st input tokens
     */
    private void openAccount(AccountDatabase acctDb, StringTokenizer st) {
        try {
            String acctType = st.nextToken();
            String firstName = st.nextToken(), lastName = st.nextToken();
            StringTokenizer dateTokens =
                    new StringTokenizer(st.nextToken(), "/");
            int month = Integer.parseInt(dateTokens.nextToken()),
                    day = Integer.parseInt(dateTokens.nextToken()),
                    year = Integer.parseInt(dateTokens.nextToken());
            Date dateOfBirth = new Date(year, month, day);
            double deposit = Double.parseDouble(st.nextToken());
            Campus campus = null;
            boolean isLoyal = false;
            if (acctType.equals("CC")) {
                campus = findCampus(Integer.parseInt(st.nextToken()));
            }
            if (acctType.equals("S")) {
                isLoyal = st.nextToken().equals("1");
            }

            if (!isValidCredentials(acctType, dateOfBirth, deposit, campus)) {
                return;
            }
            Profile profile = new Profile(firstName, lastName, dateOfBirth);
            Account acct = makeAcct(acctType, profile, deposit,
                    campus, isLoyal);
            if (isValidOpenAcct(acctDb, acct, acctType)) {
                acctDb.open(acct);
                System.out.println(profile + "(" + acctType + ") opened.");
            }
        }
        catch (NoSuchElementException exp) {
            System.out.println("Missing data for opening an account.");
        }
        catch (NumberFormatException exp) {
            System.out.println("Not a valid amount.");
        }
    }

    /**
     * Closes an account using the specified input
     * @param acctDb the account database
     * @param st input tokens
     */
    private void closeAccount(AccountDatabase acctDb, StringTokenizer st) {
        try {
            String acctType = st.nextToken();
            String firstName = st.nextToken(), lastName = st.nextToken();
            StringTokenizer dateTokens =
                    new StringTokenizer(st.nextToken(), "/");
            int month = Integer.parseInt(dateTokens.nextToken()),
                    day = Integer.parseInt(dateTokens.nextToken()),
                    year = Integer.parseInt(dateTokens.nextToken());
            Date dateOfBirth = new Date(year, month, day);
            Profile profile =  new Profile(firstName, lastName, dateOfBirth);
            if (!isValidDob(dateOfBirth)) {
                return;
            }

            Account acct = makeAcct(acctType, profile);

            if (acctDb.close(acct)) {
                System.out.println(profile + "(" + acctType + ") has been closed.");
            }
            else {
                System.out.println(profile + "(" + acctType + ") is not in the database.");
            }
        }
        catch (NoSuchElementException exp) {
            System.out.println("Missing data for closing an account.");
        }
    }

    /**
     * Deposits the specified amount into the account if possible
     * @param acctDb  the account database
     * @param st input tokens
     */
    private void deposit(AccountDatabase acctDb, StringTokenizer st) {
        try {
            String acctType = st.nextToken();
            String firstName = st.nextToken(), lastName = st.nextToken();
            StringTokenizer dateTokens =
                    new StringTokenizer(st.nextToken(), "/");
            int month = Integer.parseInt(dateTokens.nextToken()),
                    day = Integer.parseInt(dateTokens.nextToken()),
                    year = Integer.parseInt(dateTokens.nextToken());
            Date dateOfBirth = new Date(year, month, day);
            double deposit = Double.parseDouble(st.nextToken());
            Profile profile =  new Profile(firstName, lastName, dateOfBirth);
            if (!isValidDob(dateOfBirth)) {
                return;
            }
            if (deposit <= EMPTY) {
                System.out.println("Deposit - amount cannot be "
                        + "0 or negative.");
                return;
            }
            Account acct = makeAcct(acctType, profile, deposit);
            if (acctDb.contains(acct)) {
                acctDb.deposit(acct);
                System.out.println(profile + "(" + acctType +
                        ") Deposit - balance updated.");
            }
            else {
                System.out.println(profile + "(" + acctType +
                        ") is not in the database.");
            }
        }
        catch (NoSuchElementException exp) {
            System.out.println("Missing data for depositing into "
                    + "an account.");
        }
        catch (NumberFormatException exp) {
            System.out.println("Not a valid amount.");
        }
    }

    /**
     * Withdraws the specified amount from the account if possible
     * @param acctDb  the account database
     * @param st input tokens
     */
    private void withdraw(AccountDatabase acctDb, StringTokenizer st) {
        try {
            String acctType = st.nextToken();
            String firstName = st.nextToken(), lastName = st.nextToken();
            StringTokenizer dateTokens =
                    new StringTokenizer(st.nextToken(), "/");
            int month = Integer.parseInt(dateTokens.nextToken()),
                    day = Integer.parseInt(dateTokens.nextToken()),
                    year = Integer.parseInt(dateTokens.nextToken());
            Date dateOfBirth = new Date(year, month, day);
            double amount = Double.parseDouble(st.nextToken());
            Profile profile =  new Profile(firstName, lastName, dateOfBirth);
            if (!isValidDob(dateOfBirth)) {
                return;
            }
            if (amount <= EMPTY) {
                System.out.println("Withdraw - amount cannot be "
                        + "0 or negative.");
                return;
            }
            isWithdrawable(acctDb, acctType, profile, amount);
        }
        catch (NoSuchElementException exp) {
            System.out.println("Missing data for withdrawing from an account.");
        }
        catch (NumberFormatException exp) {
            System.out.println("Not a valid amount.");
        }
    }

    /**
     * Checks whether if the balance is withdrawn properly and
     * prints a message based on the action done
     * @param acctDb the account database
     * @param acctType account type
     * @param profile account holder's profile
     */
    private void isWithdrawable(AccountDatabase acctDb, String acctType,
                                Profile profile, double amount) {
        Account acct = makeAcct(acctType, profile, amount);
        if (acctDb.contains(acct)) {
            if (acctDb.withdraw(acct)) {
                System.out.println(profile + "(" + acctType +
                        ") Withdraw - balance updated.");
            }
            else {
                System.out.println(profile + "(" + acctType +
                        ") Withdraw - insufficient fund.");
            }
        }
        else {
            System.out.println(profile + "(" + acctType +
                    ") is not in the database.");
        }
    }

    /**
     * Checks whether the input date of birth is valid
     * @param dob date of birth of account holder
     * @return true if dob is valid, false otherwise
     */
    private boolean isValidDob(Date dob) {
        if (!dob.isValid()) {
            System.out.println("DOB invalid: " + dob
                    + " not a valid calendar date!");
            return false;
        }
        if (dob.isToday_Or_FutureDate()) {
            System.out.println("DOB invalid: " + dob
                    + " cannot be today or a future day.");
            return false;
        }
        return true;
    }

    /**
     * Checks whether the input credentials are valid
     * @param acctType account type
     * @param dob date of birth of account holder
     * @param deposit initial deposit
     * @param campus campus of account holder if college checking
     * @return true if credentials are valid, false otherwise
     */
    private boolean isValidCredentials(String acctType, Date dob,
                                double deposit, Campus campus) {
        if (!isValidDob(dob)) {
            return false;
        }
        if (dob.getAge() < MIN_AGE) {
            System.out.println("DOB invalid: " + dob
                    + " under 16.");
            return false;
        }
        if (deposit <= EMPTY) {
            System.out.println("Initial deposit cannot be "
                    + "0 or negative.");
            return false;
        }
        if (acctType.equals("CC")) {
            if (campus == null) {
                System.out.println("Invalid campus code.");
                return false;
            }
            if (dob.getAge() >= COLLEGE_MAX_AGE) {
                System.out.println("DOB invalid: " + dob
                        + " over 24.");
                return false;
            }
        }
        if (acctType.equals("MM") && deposit < MIN_MONEYMARKET_DEPOSIT) {
            System.out.println("Minimum of $2000 to open a "
                    + "Money Market account.");
            return false;
        }
        return true;
    }

    /**
     * Creates an account using the specified attributes
     * @param acctType account type
     * @param profile account holder's profile
     * @param deposit initial deposit
     * @param campus campus of account holder if college checking
     * @param isLoyal boolean representing whether a
     *                savings account holder is loyal
     * @return account object
     */
    private Account makeAcct(String acctType, Profile profile,
                             double deposit, Campus campus,
                             boolean isLoyal) {
        switch (acctType) {
            case "C":
                return new Checking(profile, deposit);
            case "CC":
                return new CollegeChecking(profile, deposit, campus);
            case "S":
                return new Savings(profile, deposit, isLoyal);
            case "MM":
                return new MoneyMarket(profile, deposit);
            default:
                return null;
        }
    }

    /**
     * Creates an account using the specified attributes
     * @param acctType account type
     * @param profile account holder's profile
     * @param amount amount to be deposited or withdrawn
     * @return account object
     */
    private Account makeAcct(String acctType, Profile profile,
                             double amount) {
        switch (acctType) {
            case "C":
                return new Checking(profile, amount);
            case "CC":
                return new CollegeChecking(profile, amount);
            case "S":
                return new Savings(profile, amount);
            case "MM":
                return new MoneyMarket(profile, amount);
            default:
                return null;
        }
    }

    /**
     * Creates an account using the specified attributes
     * @param acctType account type
     * @param profile account holder's profile
     * @return account object
     */
    private Account makeAcct(String acctType, Profile profile) {
        switch (acctType) {
            case "C":
                return new Checking(profile);
            case "CC":
                return new CollegeChecking(profile);
            case "S":
                return new Savings(profile);
            case "MM":
                return new MoneyMarket(profile);
            default:
                return null;
        }
    }

    /**
     * Checks if whether the account to be opened exists
     * and prints so if it does
     * @param acctDb the account database
     * @param acct the account
     * @param acctType the account type
     */
    private boolean isValidOpenAcct(AccountDatabase acctDb, Account acct,
                             String acctType) {
        boolean isOpening = true;
        if (acctDb.contains(acct, isOpening)) {
            System.out.println(acct.getProfile() + "(" + acctType
                    + ") is already in the database.");
            return false;
        }
        return true;
    }

    /**
    * Returns enum campus that corresponds to input campus
    * if there is one
    * @param campus the input campus
    * @return enum campus if it exists, null otherwise
    */
    private Campus findCampus(int campus) {
        for (Campus c: Campus.values()) {
            if (c.ordinal() == campus) {
                return c;
            }
        }
        return null;
    }
}