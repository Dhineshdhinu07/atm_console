package com.company;

import java.time.LocalDate;
import java.util.*;

class User {
    String id;
    String pass;
    int pin;
    double balance;
    ArrayList<String> transaction = new ArrayList<String>();

    User(String id, String pass, int pin) {
        this.id = id;
        this.pass = pass;
        this.pin = pin;
        this.balance = 1000.0;
    }

    void checkBalance() {
        System.out.println("\nAvailable Balance : Rs." + (this.balance));
    }

    void deposit(double amt) {
        boolean flag = Main.atm.checkDenomination(amt);

        if (flag) {
            System.out.println("\nOld Balance : Rs." + balance);
            balance += amt;
            addTransaction("Deposit : Rs." + amt + "\nBalance : Rs." + balance);
            System.out.println("Available Balance : Rs." + balance);
        } else {
            System.out.println("\nERROR : Invalid Denomination!!");
            deposit(amt);
        }
    }

    void pinChange(int pin) {
        if (pin < 1000 || pin > 9999) System.out.println("\nERROR : PIN should be 4 digits!!");
        else {
            if (pin == this.pin) System.out.println("\nERROR : New PIN should not be same as old PIN!!");
            else {
                this.pin = pin;
                System.out.println("\nPIN changed Successfully !!\nCurrent PIN : " + pin);
            }
        }
    }

    void withdraw(double amt, int pin, ATM atm) {
        if (pin != this.pin) System.out.println("\nERROR : Invalid PIN !!");
        else {
            if (amt > balance) System.out.println("\nERROR : Insufficient Account Balance!!");
            else {
                double remainingAmount = amt;
                HashMap<Integer, Integer> notesDispensed = new HashMap<Integer, Integer>();
                for (int note : new int[]{2000, 500, 200, 100}) {
                    int notesNeeded = (int) (remainingAmount / note);
                    int notesAvailable = Main.atm.denominations.get(note);
                    double notesDispensedForNote = Math.min(notesNeeded, notesAvailable);
                    notesDispensed.put(note, (int) notesDispensedForNote);
                    remainingAmount -= notesDispensedForNote * note;
                }
                if (remainingAmount != 0) {
                    System.out.println("Unable to dispense requested amount. Transaction cancelled.");
                    return;
                }
                for (int note : notesDispensed.keySet()) {
                    Main.atm.denominations.put(note, Main.atm.denominations.get(note) - notesDispensed.get(note));
                }
                Main.atm.stock-=amt;
                balance -= amt;
                addTransaction("Withdrawal : Rs." + amt + "\nBalance : Rs." + balance);
                System.out.println("\nAmount dispensed: Rs." + amt);
                System.out.println("\nCurrency    Count");
                System.out.println("-------     -----");
                System.out.printf("%-5d        %-5d\n", 2000, notesDispensed.get(2000));
                System.out.printf("%-5d        %-5d\n", 500, notesDispensed.get(500));
                System.out.printf("%-5d        %-5d\n", 200, notesDispensed.get(200));
                System.out.printf("%-5d        %-5d\n", 100, notesDispensed.get(100));
            }
        }
    }

    void addTransaction(String trn) {
        if (transaction.size() == 5) transaction.remove(0);
        transaction.add("Date : " + (LocalDate.now()) + "\n" + trn);
    }

    void getStatement() {
        if (transaction.size() == 0) System.out.println("\nNo Transaction History!!");
        else {
            System.out.println("\n-----Mini Statement-----");
            for (String str : transaction) System.out.println(str + "\n");
            System.out.println("------------------------");
        }
    }

    void transfer(double amt, User user, ATM atm) {
        System.out.println("\nSuccessfully Transferred Rs." + amt + " to " + user.id);
        balance -= amt;
        user.balance += amt;
        System.out.println("Available Balance : Rs." + balance);
        addTransaction("Transfer Amount : Rs." + amt + "\nBeneficiary : " + user.id + "\nBalance : Rs." + balance);
        user.addTransaction("Received Amount : Rs." + amt + "\nFrom : " + id + "\nBalance : Rs." + user.balance);
    }

}

class Admin {
    String id;
    String pass;

    Admin(String id, String pass) {
        this.id = id;
        this.pass = pass;
    }

    void createUser(String id, String pass, int pin) {
        User user = new User(id, pass, pin);
        Main.atm.user_map.put(user.id, user);
        System.out.println("User added Successfully!!");
    }

    void reStock(double amt) {
        Scanner scn = new Scanner(System.in);
        ATM atm = Main.atm;

        boolean flag = atm.checkDenomination(amt);
        if (flag) {
            System.out.println("Old Balance : Rs." + (atm.stock - amt));
            System.out.println("New Balance : Rs." + atm.stock);
            System.out.println("Restocked Rs." + amt + " Successfully!!");
        } else {
            System.out.println("ERROR : Invalid Denomination !!");
            reStock(amt);
        }
    }

    void checkStock() {
        System.out.println("\nBalance Stock in the ATM : Rs." + Main.atm.stock);
        System.out.println("\nCurrency    Count");
        System.out.println("-------     -----");
        System.out.printf("%-5d        %-5d\n", 2000, Main.atm.denominations.get(2000));
        System.out.printf("%-5d        %-5d\n", 500, Main.atm.denominations.get(500));
        System.out.printf("%-5d        %-5d\n", 200, Main.atm.denominations.get(200));
        System.out.printf("%-5d        %-5d\n", 100, Main.atm.denominations.get(100));
    }
}

class ATM {
    double stock = 0;
    String curr_login = null;
    HashMap<String, User> user_map = new HashMap<String, User>();
    HashMap<Integer, Integer> denominations = new HashMap<Integer, Integer>();
    Admin admin = null;
    Scanner scn = new Scanner(System.in);

    ATM() {
        denominations.put(100, 0);
        denominations.put(200, 0);
        denominations.put(500, 0);
        denominations.put(2000, 0);
    }

    boolean checkDenomination(double amt) {
        System.out.print("\nEnter Number of 100's : ");
        int hun = scn.nextInt();

        System.out.print("Enter Number of 200's : ");
        int two_hun = scn.nextInt();

        System.out.print("Enter Number of 500's : ");
        int five_hun = scn.nextInt();

        System.out.print("Enter Number of 2000's : ");
        int two_thou = scn.nextInt();

        double total = (100 * hun) + (200 * two_hun) + (500 * five_hun) + (2000 * two_thou);

        if (total == amt) {
            stock += amt;
            denominations.put(100, denominations.get(100) + hun);
            denominations.put(200, denominations.get(200) + two_hun);
            denominations.put(500, denominations.get(500) + five_hun);
            denominations.put(2000, denominations.get(2000) + two_thou);
            return true;
        }
        return false;
    }

}

public class Main {

    static ATM atm = new ATM();

    static String ask_first() {
        String admin_id = "admin", admin_pass = "admin123";
        Scanner scn = new Scanner(System.in);

        System.out.println("\n----- ATM Console -----\n    1. Admin\n    2. User\n    3. EXIT");
        System.out.print("\nLogin as : ");
        int op = scn.nextInt();

        if (op == 1) {
            System.out.println("\n----- ADMIN Login -----\n");
            System.out.print("Enter Admin ID : ");
            String id = scn.next();
            System.out.print("Enter Admin Password : ");
            String pass = scn.next();

            if (!(id.equals(admin_id) && pass.equals(admin_pass))) {
                System.out.println("ERROR : Invalid Credentials !!");
                return "null";
            }

            atm.admin = new Admin(id, pass);
            atm.curr_login = "admin";
            return "admin";
        } else if (op == 2) {

            if (atm.admin == null) {
                System.out.println("ERROR : No Admin Found!!");
                return "null";
            }

            System.out.println("\n----- USER Login -----\n");
            System.out.print("Enter user ID : ");
            String id = scn.next();
            System.out.print("Enter User Password : ");
            String pass = scn.next();
            if (!atm.user_map.containsKey(id)) {
                System.out.println("ERROR : No User Found!!");
                return "null";
            } else {
                if (!((atm.user_map.get(id).pass).equals(pass))) {
                    System.out.println("ERROR : Invalid Password!!");
                    return "null";
                }
            }

            atm.curr_login = id;
            return id;

        } else {
            return "exit";
        }

    }

    public static void main(String[] args) {
        Scanner scn = new Scanner(System.in);

        while (true) {
//            try {
                String cur_login = ask_first();

                if (cur_login.equals("admin")) {

                    while (true) {
                        System.out.println("\n1. Add User");
                        System.out.println("2. Re-Stock");
                        System.out.println("3. Check Stock");
                        System.out.println("4. Exit");
                        System.out.print("\nChoose an Operation : ");

                        int op = scn.nextInt();

                        switch (op) {
                            case 1:
                                String username, pass;
                                int pin;
                                System.out.print("\nEnter Username : ");
                                scn.nextLine();
                                username = scn.nextLine();

                                if (atm.user_map.containsKey(username)) {
                                    System.out.println("ERROR : Username Already Exists!!");
                                } else {
                                    System.out.print("Enter Password : ");
                                    pass = scn.nextLine();
                                    System.out.print("Enter PIN : ");
                                    pin = scn.nextInt();
                                    if (pin < 1000 || pin > 9999) {
                                        System.out.println("ERROR : PIN should be 4 digit!!");
                                    } else atm.admin.createUser(username, pass, pin);
                                }
                                break;

                            case 2:
                                System.out.print("\nEnter Restock Amount : ");
                                double amt = scn.nextDouble();
                                atm.admin.reStock(amt);
                                break;

                            case 3:
                                atm.admin.checkStock();
                                break;

                            case 4:
                                atm.curr_login = null;
                                System.out.println("Logging Out !!");
                                break;
                        }

                        if (atm.curr_login == null) break;
                    }
                } else if (cur_login.equals("exit")) {
                    System.out.println("Shutting the System Down!!");
                    break;
                } else if (cur_login.equals("null")) ;

                else {
                    User user = atm.user_map.get(cur_login);

                    while (true) {
                        System.out.println("\n1. Check Balance");
                        System.out.println("2. Withdraw Cash");
                        System.out.println("3. Deposit Cash");
                        System.out.println("4. Statement");
                        System.out.println("5. Transfer Cash");
                        System.out.println("6. PIN change");
                        System.out.println("7. Exit");
                        System.out.print("\nChoose an Operation : ");

                        double amt;
                        int pin;
                        int op = scn.nextInt();

                        switch (op) {
                            case 1:
                                user.checkBalance();
                                break;

                            case 2:
                                System.out.print("\nEnter the Amount : ");
                                amt = scn.nextDouble();
                                System.out.print("Enter the PIN : ");
                                pin = scn.nextInt();
                                if (amt > atm.stock) System.out.println("\nERROR : Insufficient ATM Cash!!");
                                else user.withdraw(amt, pin, atm);
                                break;

                            case 3:
                                System.out.print("\nEnter the amount : ");
                                amt = scn.nextDouble();
                                user.deposit(amt);
                                break;

                            case 4:
                                user.getStatement();
                                break;

                            case 5:
                                System.out.print("\nEnter Amount : ");
                                amt = scn.nextDouble();
                                System.out.print("Enter Beneficiary Name : ");
                                String name = scn.next();
                                if (!(atm.user_map.containsKey(name))) {
                                    System.out.println("\nERROR : No User Found!!");
                                } else {
                                    if (amt > user.balance) System.out.println("\nERROR : Insufficient Balance!!");
                                    else user.transfer(amt, atm.user_map.get(name), atm);
                                }
                                break;

                            case 6:
                                System.out.println("\nEnter New PIN : ");
                                pin = scn.nextInt();
                                user.pinChange(pin);
                                break;

                            case 7:
                                atm.curr_login = null;
                                System.out.println("Logging Out !!");
                                break;
                        }

                        if (atm.curr_login == null) break;
                    }
                }
//            }
//            catch (Exception e) {
//                System.out.println("ERROR : Invalid Input!!");
//            }
        }
    }
}