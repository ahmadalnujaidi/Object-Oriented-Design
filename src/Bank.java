import java.util.*;
class Main {
    public static void main(String[] args){
        BankSystem bankSystem = new BankSystem(new ArrayList<BankAccount>(), new ArrayList<Transaction>());
        Bank bank = new Bank(new ArrayList<BankBranch>(), bankSystem,10000);

        BankBranch branch1 = bank.addBranch("123 main st", 1000);
        BankBranch branch2 = bank.addBranch("456 elm st", 1000);

        branch1.addTeller(new BankTeller(1));
        branch1.addTeller(new BankTeller(2));
        branch2.addTeller(new BankTeller(3));
        branch2.addTeller(new BankTeller(4));

        int customerId1 = branch1.openAccount("John Doe");
        int customerId2 = branch1.openAccount("Bob smith");
        int customerId3 = branch2.openAccount("Ahmad Alnujaidi");

        branch1.deposit(customerId1, 100);
        branch1.deposit(customerId2, 200);
        branch2.deposit(customerId3, 300);

        branch1.withdraw(customerId1, 50);

        bank.printTransactions();
        bank.collectCash(0.5);
    }
}
class Transaction {
    private int customerId;
    private int tellerId;

    public Transaction(int customerId, int tellerId){
        this.customerId = customerId;
        this.tellerId = tellerId;
    }

    public int getCustomerId(){
        return this.customerId;
    }
    public int getTellerId(){
        return this.tellerId;
    }
    public String getTransactionDescription(){
        return "";
    }
}

class Deposit extends Transaction {
    private int amount;
    public Deposit(int customerId, int tellerId, int amount){
        super(customerId, tellerId);
        this.amount = amount;
    }

    @Override
    public String getTransactionDescription(){
        return "Teller " + getTellerId() + " deposited " + amount + " to account " + getCustomerId();
    }
}

class Withdrawal extends Transaction {
    private int amount;
    public Withdrawal(int tellerId, int customerId, int amount){
        super(customerId, tellerId);
        this.amount = amount;
    }
    @Override
    public String getTransactionDescription(){
        return "Teller " + getTellerId() + " withdrew " + amount + " from account " + getCustomerId();
    }
}

class OpenAccount extends Transaction {
    public OpenAccount(int customerId, int tellerId){
        super(customerId, tellerId);
    }

    @Override
    public String getTransactionDescription(){
        return "Teller " + getTellerId() + " opened account " + getCustomerId();
    }
}

class BankTeller {
    private int id;
    public BankTeller(int id){
        this.id = id;
    }
    public int getId(){
        return this.id;
    }
}

class BankAccount {
    private int customerId;
    private String name;
    private int balance;

    public BankAccount(int customerId, String name, int balance){
        this.customerId = customerId;
        this.name = name;
        this.balance = balance;
    }

    public int getBalance(){
        return this.balance;
    }
    public void deposit(int amount){
        this.balance += amount;
    }
    public void withdraw(int amount){
        this.balance -= amount;
    }
}
class BankSystem {
    private List<BankAccount> accounts;
    private List<Transaction> transactions;

    public BankSystem(List<BankAccount> accounts, List<Transaction> transactions){
        this.accounts = accounts;
        this.transactions = transactions;
    }
    public BankAccount getAccount(int customerId){
        return this.accounts.get(customerId);
    }
    public List<BankAccount> getAccounts(){
        return this.accounts;
    }
    public List<Transaction> getTransactions(){
        return this.transactions;
    }

    public int openAccount(String customerName, int tellerId){
        //create acc
        int customerId = this.accounts.size();
        BankAccount account = new BankAccount(customerId, customerName, 0);
        this.accounts.add(account);

        // log transaction
        Transaction transaction = new OpenAccount(customerId, tellerId);
        this.transactions.add(transaction);
        return customerId;
    }
    public void deposit(int customerId, int tellerId, int amount){
        BankAccount account = this.getAccount(customerId);
        account.deposit(amount);

        // log transaction
        Transaction transaction = new Deposit(customerId, tellerId, amount);
        this.transactions.add(transaction);
    }
    public void withdraw(int customerId, int tellerId, int amount){
        if (amount > this.getAccount(customerId).getBalance()){
            throw new Error("insufficient funds");
        }
        BankAccount account = this.getAccount(customerId);
        account.withdraw(amount);

        // log
        Transaction transaction = new Withdrawal(customerId, tellerId, amount);
        this.transactions.add(transaction);
    }
}

class BankBranch {
    private String address;
    private int cashOnHand;
    private BankSystem bankSystem;
    private List<BankTeller> tellers;

    public BankBranch(String address, int cashOnHand, BankSystem bankSystem){
        this.address = address;
        this.cashOnHand = cashOnHand;
        this.bankSystem = bankSystem;
        this.tellers = new ArrayList<>();
    }

    public void addTeller(BankTeller teller){
        this.tellers.add(teller);
    }
    private BankTeller getAvailableTeller(){
        int index = (int) Math.round(Math.random() * (this.tellers.size() - 1));
        return this.tellers.get(index);
    }
    public int openAccount(String customerName){
        if(this.tellers.size() == 0){
            throw new Error("branch has no tellers");
        }
        BankTeller teller = this.getAvailableTeller();
        return this.bankSystem.openAccount(customerName, teller.getId());
    }
    public void deposit(int customerId, int amount){
        if(this.tellers.size() == 0){
            throw new Error("bank no tellers");
        }
        BankTeller teller = this.getAvailableTeller();
        this.bankSystem.deposit(customerId, teller.getId(), amount);
    }
    public void withdraw(int customerId, int amount){
        if(amount > this.cashOnHand){
            throw new Error("Branch not enough money");
        }
        if(this.tellers.size() == 0){
            throw new Error("bank no tellers");
        }
        this.cashOnHand -= amount;
        BankTeller teller = this.getAvailableTeller();
        this.bankSystem.withdraw(customerId, teller.getId(), amount);
    }
    public int collectCash(double ratio){
        int cashToCollect = (int) Math.round(this.cashOnHand * ratio);
        this.cashOnHand -= cashToCollect;
        return cashToCollect;
    }
    public void provideCash(int amount){
        this.cashOnHand += amount;
    }
}

public class Bank {
    private List<BankBranch> branches;
    private BankSystem bankSystem;
    private int totalCash;

    public Bank(List<BankBranch> branches, BankSystem bankSystem, int totalCash){
        this.branches = branches;
        this.bankSystem = bankSystem;
        this.totalCash = totalCash;
    }

    public BankBranch addBranch(String address, int initialFunds){
        BankBranch branch = new BankBranch(address, initialFunds, this.bankSystem);
        this.branches.add(branch);
        return branch;
    }
    public void collectCash(double ratio){
        for(BankBranch branch: branches){
            int cashCollected = branch.collectCash(ratio);
            this.totalCash += cashCollected;
        }
    }

    public void printTransactions(){
        for(Transaction transaction: this.bankSystem.getTransactions()){
            System.out.println(transaction.getTransactionDescription());
        }
    }
}