package dev.zinebmorad;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        RAM ram = new RAM() ;
        /*
        ram.WriteInMemory(1,13);
        ram.ReadFromMemory(1) ;
        System.out.println("value : " + ram.ReadFromMemory(1));*/

        //TEST DATE: 3/11/2025
        CPU cpu = new CPU(ram) ;
        /*
        ram.WriteWord(0xFFFE,0xC000);
        System.out.printf("%s",ram.ReadFromMemory(0x1000)) ;
        int hight = ram.ReadFromMemory(0x1000);
        int low = ram.ReadFromMemory(0x1001);
        System.out.println(Integer.toHexString(hight));
        System.out.println(Integer.toHexString(low) );*/

        //CPU cpu1 = new CPU(ram) ;
        //System.out.println(Integer.toHexString(cpu1.getPC()));

        //TEST DATE : 04/11/2025 // for this test we need to make the methodes we work with puplic
        /*
        cpu1.DP = 0x10 ;
        ram.WriteInMemory(0xC000,0x50);
        int ea = cpu1.getDirectAdress() ;
        System.out.println(Integer.toHexString(ea)) ;
        System.out.println(Integer.toHexString(cpu1.getPC())) ;*/
        /*int ae = cpu.fetchWord() ;
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
        System.out.print(ae);*/
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


    }
}