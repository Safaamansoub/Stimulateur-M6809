package dev.zinebmorad;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        RAM ram = new RAM() ;
        ram.WriteInMemory(1,13);
        ram.ReadFromMemory(1) ;
        System.out.println("value : " + ram.ReadFromMemory(1));

    }
}