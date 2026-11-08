import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int choice = -1;
        while (choice != 0) {
            printMenu();
            String line = in.nextLine().trim();
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number from the menu.");
                continue;
            }
            switch (choice) {
                case 1 -> System.out.println("Load input data: not implemented yet.");
                case 2 -> System.out.println("Generate timetable: not implemented yet.");
                case 0 -> System.out.println("Goodbye.");
                default -> System.out.println("Unknown option: " + choice);
            }
        }
        in.close();
    }

    private static void printMenu() {
        System.out.println("==================================================");
        System.out.println(" DEPARTMENT COURSE TIMETABLE MANAGEMENT SYSTEM");
        System.out.println("==================================================");
        System.out.println(" 1. Load input data");
        System.out.println(" 2. Generate timetable automatically");
        System.out.println(" 0. Exit");
        System.out.print("Select an option: ");
    }
}