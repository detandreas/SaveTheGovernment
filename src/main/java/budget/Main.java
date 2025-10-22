package budget;

import java.util.Scanner;

/**
 * Entry point της εφαρμογής CLI.
 */
public final class Main {

    private Main() { }
    /**
     * Main method της εφαρμογής CLI.
     * @param args ορίσματα γραμμής εντολών
     */
    public static void main(final String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String name = input.nextLine();
        System.out.println("Hello, " + name + "!");
        input.close();
    }
}
