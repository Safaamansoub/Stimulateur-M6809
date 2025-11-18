/*package dev.zinebmorad;
import java.util.Scanner;
import static dev.zinebmorad.CPU.loadTestProgram;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        /*RAM ram = new RAM() ;
        ram.WriteInMemory(1,13);
        ram.ReadFromMemory(1) ;
        System.out.println("value : " + ram.ReadFromMemory(1));

        //TEST DATE: 3/11/2025
        CPU cpu = new CPU(ram) ;

        ram.WriteWord(0xFFFE,0xC000);
        System.out.printf("%s",ram.ReadFromMemory(0x1000)) ;
        int hight = ram.ReadFromMemory(0x1000);
        int low = ram.ReadFromMemory(0x1001);
        System.out.println(Integer.toHexString(hight));
        System.out.println(Integer.toHexString(low) );

        //CPU cpu1 = new CPU(ram) ;
        //System.out.println(Integer.toHexString(cpu1.getPC()));

        //TEST DATE : 04/11/2025 // for this test we need to make the methodes we work with puplic

        cpu1.DP = 0x10 ;
        ram.WriteInMemory(0xC000,0x50);
        int ea = cpu1.getDirectAdress() ;
        System.out.println(Integer.toHexString(ea)) ;
        System.out.println(Integer.toHexString(cpu1.getPC())) ;
        int ae = cpu.fetchWord() ;
        System.out.print(ae);
        ae = cpu.getPC() ;
        System.out.print(ae);
        cpu.reset();
        int ae = cpu.getA() ;
        System.out.print(ae);
         ae = cpu.getB() ;
        System.out.print(ae);
          ae = cpu.getX() ;
        System.out.print(ae);
         ae = cpu.getY() ;
        System.out.print(ae);
        int a = cpu.fetchWord();
        System.out.println(a);
        ram.WriteWord(1,202255452);
        a = cpu.fetchWord();
        System.out.println(a);
        a = cpu.fetchWord();
        System.out.println(a);
        a=ram.ReadWord(1);
        System.out.println(a);
        a=cpu.getA();
        System.out.println(a);
       //a= cpu.getDirectAdress();
        System.out.println(a);

        //TEST DATE : 5/11/2025
        //for testing the getD setD should make A and B public

        cpu.A = 00000 ;
        cpu.B = 255;
        int D = cpu.getD() ;
        System.out.println(D);
        cpu.setD(256);
        D = cpu.getD() ;
        System.out.println(D);


        //TEST DATE 6/11/2025
        // this test is for the instructions functions we cant just change private to public to test it cause its already needing hidden dependcy the PC
        // so for that we need to implement the big switch to see the comportomont
        // but first the function that get all the registres
        loadTestProgram(ram);
        ram.WriteWord(0xFFFE, 0xC000);
        cpu.reset();
        System.out.println("--- CPU STATE BEFORE ---");
        cpu.printAllRegisters();
        // 5. Run the CPU for ~30 steps
        // This loop will call your step() method repeatedly
        System.out.println("\n--- RUNNING PROGRAM ---");
        for (int i = 0; i < 30; i++) {
            // Stop if we hit the 'HALT' loop
            if (cpu.getPC() == 0xC01A) {
                System.out.println("Program halted at $C01A.");
                break;
            }
            System.out.println("Step " + i + ": Executing at $" + Integer.toHexString(cpu.getPC()));
            cpu.step(); // This is the new method you will add
        }

        // 6. Print the final state of the registers
        System.out.println("\n--- CPU STATE AFTER ---");
        cpu.printAllRegisters();

        // 7. Verify the results
        System.out.println("\n--- TEST VERIFICATION ---");
        cpu.verifyResults(cpu);





        RAM ram = new RAM();
        CPU cpu = new CPU(ram);
        /* AFFICHAGE DE MENU DOPERATIONS POSSIPLES
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=== 6809 INTERACTIVE MONITOR ===");
        System.out.println("Commands:");
        System.out.println("  WRITE <addr> <byte>  : Write data to RAM (e.g., WRITE C000 86)");
        System.out.println("  RUN <addr>           : Set PC to addr and execute one step");
        System.out.println("  EXIT                 : Quit");

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split(" ");
            String command = parts[0].toUpperCase();

            try {
                if (command.equals("EXIT")) {
                    break;
                }
                else if (command.equals("WRITE")) {
                    // Logic: Write user input into RAM
                    if (parts.length < 3) {
                        System.out.println("Error: Usage WRITE <addr> <byte>");
                        continue;
                    }
                    int address = Integer.parseInt(parts[1], 16); // Parse Hex Address
                    int value = Integer.parseInt(parts[2], 16);   // Parse Hex Value
                    ram.WriteInMemory(address, value);
                    System.out.printf("Written $%02X to address $%04X%n", value, address);
                }
                else if (command.equals("RUN")) {
                    // Logic: Point PC to address and call step() (The Switch)
                    if (parts.length < 2) {
                        System.out.println("Error: Usage RUN <addr>");
                        continue;
                    }
                    int startAddress = Integer.parseInt(parts[1], 16);

                    // 1.  nhto PC f ladresse li d5l syd
                    cpu.setPC(startAddress);

                    System.out.printf("Executing instruction at $%04X...%n", startAddress);

                    cpu.printAllRegisters();
                    // 2. n3yto ela switch
                    cpu.step();

                    // nprintiwe kolxii
                    cpu.printAllRegisters();
                }
                else {
                    System.out.println("Unknown command.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Check your Hex values (use 0-9, A-F)");
            }
        }
        scanner.close();




    }
}

*/