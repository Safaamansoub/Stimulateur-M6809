package dev.zinebmorad;

public class CPU {

    // ETAPE 1 : DECLARATION PART

    // we need a reference to the memory
    private RAM ram;


    // declaration of the motorola's REGISTERS :
    // Accumulators
    public int A;
    public int B;
    // private int D ; im not gonna declared it cause its gonna be like a couple of A and B
    // Index Registres
    private int X;
    private int Y;
    //Pointer Registres
    private int U; //user stack pointer
    private int S; //System stack pointer
    //Program Counter
    private int PC;
    //Direct page pointer
    public int DP ;

    //Condition code register // his job is to hold 8 flags (status bits)
    private int CC ;

    // declaration of the flags :
    public  static final int C_FLAG = 0x01 ;
    public  static final int V_FLAG = 0x02 ;
    public  static final int Z_FLAG = 0x04 ;
    public  static final int N_FLAG = 0x08 ;
    public  static final int I_FLAG = 0x10 ;
    public  static final int H_FLAG = 0x20 ;
    public  static final int F_FLAG = 0x40 ;
    public  static final int E_FLAG = 0x80 ;

    // ETAPE 2 : THE FUNCTIONS HELPERS :
    //  Fct for reseting all the values of the registers to 0
    public void reset (){
        A = 0 ;
        B = 0 ;
        X = 0 ;
        Y = 0 ;
        U = 0 ;
        S = 0 ;
        DP = 0 ;
        CC = 0 ;

        // For the counter
        // this needs to be with the ram but first we need the constructor for the ram created there
        PC = ram.ReadWord(0xFFFE);
        //for the 16-bit adress goes from FFFE ( HIGHT BYTE) and FFFF (low byte )
    }

    // constructor :
    public CPU(RAM ram ){
        this.ram = ram ;
        reset();//once we create we reset
    }

    // functions for print all the values of all the registres
    public int getA(){
        return A;
    }
    public int getB(){
        return B;
    }
    public int getX(){
        return X;
    }
    public int getY(){
        return Y;
    }
    public int getU(){
        return U;
    }
    public int getS(){
        return S;
    }
    public int getDP(){
        return DP ;
    }
    public int getCC() {return CC;}
    public int getPC(){
        return PC;
    }

    // functions for flags
    // we have the zero and the negatives ones are used by 80% mn les instrcutions and the others are like udpates by some special ones
    //we gonna create 3 functions that geos with any flags then functions that gonna see those most commen ones
    private void setFlag(int flag){
        CC = CC | flag ;
    }
    private  void clearFlag (int flag ){
        CC &= ~flag ;
    }
    private boolean isFlagSet( int flag){
        return (CC & flag) != 0;
    }

    // functions for the adressing modes
    // we have 6 adressing mode
    // for the inherent mode we need no function the opcode is enough
    // for the immediate mode "the data is the very next byte or word after the opcode " we
    /* for this we need to call the function fetch */
    // we add the function fetchByte and ftechWord
    public int fetchByte(){
        int value = ram.ReadFromMemory(PC);
        PC = (PC+1) & 0xFFFF ;
        return value ;
    }
    public int fetchWord(){
        int value = ram.ReadWord(PC);
        PC = (PC + 2) & 0xFFFF;
        return value ;
    }
    // for the extend mode
    /* the next 2 byte after the opcode from a full 16-bit
    we can just re-use the fetch functions */
    private int getExtendAdress(){
        return  fetchWord() ;
    }
    //Direct mode adressing
    /* the next one byte after the opcode is the law-byte of an adress . the haight
    one comes frim the DP register
    so lk we need the a fucntion that fetch a byte and combine it with the DP
    */
    private int getDirectAdress(){
         int offset = fetchByte();
         return (DP << 8) | offset ;
    }
    // for the others were gonna created them whem we need




    //for D (A+B)
    private int getD(){
        return ((A & 0xFF) << 8) | (B & 0xFF);
    }
    private void setD(int value){
        A=(value >> 8) & 0xFF;
        B=value & 0xFF;
    }
    // functions helpers for the Z and N and V flags

    private void udpateZNFlags16(int resultWord){
        // udpate the zero flag
        if((resultWord & 0xFF) == 0){
            setFlag(Z_FLAG);
        } else {
            clearFlag(Z_FLAG);
        }
        // update negative flag chek bit 15
        if ((resultWord & 0x8000) != 0){
            setFlag(N_FLAG);
        } else {
            clearFlag(N_FLAG);
        }

        // clear the overflow flag
        clearFlag(V_FLAG);
    }

    // 16-Bit LOAD AND STORE INSTRUCTIONSS


    //LDD
    // LDD IMEDIATE opcode (0xCC)
    private void ldd_immediate(){
        int worldValue = fetchWord() ;
        setD(worldValue);
        udpateZNFlags16(getD());
    }
    //LDD DIRECT opcode (0xDC)
    private void ldd_direct(){
        int ea = getDirectAdress() ;
        int wordValue = ram.ReadWord(ea);
        setD(wordValue) ;
        udpateZNFlags16(getD());
    }
    //LDD EXTEND opcode 0xFC
    private void ldd_extend(){
        int ea = getExtendAdress();
        int wordValue = ram.ReadWord(ea);
        setD(wordValue);
        udpateZNFlags16(getD());
    }
    // LDD INDEXED (Opcode: 0xEC)
    private void ldd_indexed() {
        int ea = getIndexedAddress();
        int wordValue = ram.ReadWord(ea);
        setD(wordValue);
        udpateZNFlags16(getD());
    }
    //STD
    //STD DIRECT 0xDD
    private void std_direct(){
        int ea = getDirectAdress() ;
        ram.WriteWord(ea,getD());
    }
    //STD EXTEND 0XFD
    private void std_extend(){
        int ea = getExtendAdress() ;
        ram.WriteWord(ea,getD());
    }
    // STD INDEXED (Opcode: 0xED)
    private void std_indexed() {
        int ea = getIndexedAddress();
        ram.WriteWord(ea, getD());
    }


    //LDX
    private void ldx_immediate(){
        X = fetchWord();
        udpateZNFlags16(X);
    }
    private void ldx_direct(){
        int ea = getDirectAdress() ;
        X = ram.ReadWord(ea);
        udpateZNFlags16(X);
    }
    private void ldx_extend(){
        int ea = getExtendAdress() ;
        X = ram.ReadWord(ea);
        udpateZNFlags16(X);
    }
    private void ldx_indexed() {
        int ea = getIndexedAddress();
        X = ram.ReadWord(ea);
        udpateZNFlags16(X);
    }
    //STX
    private void stx_direct(){
        int ea = getDirectAdress() ;
        ram.WriteWord(ea,X);
    }
    private void stx_extend(){
        int ea = getExtendAdress() ;
        ram.WriteWord(ea,X);
    }
    private void stx_indexed() {
        int ea = getIndexedAddress();
        ram.WriteWord(ea, X);
    }

    // LDY
    private void ldy_immediate(){
        Y = fetchWord() ;
        udpateZNFlags16(Y);
    }

    private void ldy_direct(){
        int ea = getDirectAdress() ;
        Y = ram.ReadWord(ea);
        udpateZNFlags16(Y);
    }

    private void ldy_extend(){
        int ea = getExtendAdress();
        Y = ram.ReadWord(ea);
        udpateZNFlags16(Y);
    }

    private void ldy_indexed() {
        int ea = getIndexedAddress();
        Y = ram.ReadWord(ea);
        udpateZNFlags16(Y);
    }
    //STY
    private void sty_direct(){
        int ea = getDirectAdress() ;
        ram.WriteWord(ea,Y);
    }
    private void sty_extend(){
        int ea = getExtendAdress() ;
        ram.WriteWord(ea,Y);
    }
    private void sty_indexed() {
        int ea = getIndexedAddress();
        ram.WriteWord(ea, Y);
    }

    //LDU
    private void ldu_immediate(){
        U = fetchWord();
        udpateZNFlags16(U);
    }
    private void ldu_direct(){
        int ea = getDirectAdress() ;
        U = ram.ReadWord(ea);
        udpateZNFlags16(U);
    }
    private void ldu_extend(){
        int ea = getExtendAdress() ;
        U = ram.ReadWord(ea);
        udpateZNFlags16(U);
    }
    private void ldu_indexed() {
        int ea = getIndexedAddress();
        U = ram.ReadWord(ea);
        udpateZNFlags16(U);
    }
    //STU
    private void stu_direct(){
        int ea = getDirectAdress() ;
        ram.WriteWord(ea,U);
    }
    private void stu_extend(){
        int ea = getExtendAdress() ;
        ram.WriteWord(ea,U);
    }
    private void stu_indexed() {
        int ea = getIndexedAddress();
        ram.WriteWord(ea, U);
    }



    //LDS
    private void lds_immediate(){
        S = fetchWord();
        udpateZNFlags16(S);
    }
    private void lds_direct(){
        int ea = getDirectAdress() ;
        S = ram.ReadWord(ea);
        udpateZNFlags16(S);
    }
    private void lds_extend(){
        int ea = getExtendAdress() ;
        S = ram.ReadWord(ea);
        udpateZNFlags16(S);
    }
    private void lds_indexed() {
        int ea = getIndexedAddress();
        S = ram.ReadWord(ea);
        udpateZNFlags16(S);
    }
    //STS
    private void sts_direct(){
        int ea = getDirectAdress() ;
        ram.WriteWord(ea,S);
    }
    private void sts_extend(){
        int ea = getExtendAdress() ;
        ram.WriteWord(ea,S);
    }
    private void sts_indexed() {
        int ea = getIndexedAddress();
        ram.WriteWord(ea, S);
    }

    // 16-Bit Arithmetic
    private void updateAddFlags16(int operand1, int operand2, int fullResult) {
        int result16 = fullResult & 0xFFFF;

        // Z (Zero)
        if (result16 == 0) {
            setFlag(Z_FLAG);
        } else {
            clearFlag(Z_FLAG);
        }

        // N (Negative) - check bit 15
        if ((result16 & 0x8000) != 0) {
            setFlag(N_FLAG);
        } else {
            clearFlag(N_FLAG);
        }

        // C (Carry) - check bit 16
        if ((fullResult & 0x10000) != 0) {
            setFlag(C_FLAG);
        } else {
            clearFlag(C_FLAG);
        }

        // V (Overflow) - signed overflow detection
        boolean op1Sign = (operand1 & 0x8000) != 0;
        boolean op2Sign = (operand2 & 0x8000) != 0;
        boolean resSign = (result16 & 0x8000) != 0;

        if (op1Sign == op2Sign && op1Sign != resSign) {
            setFlag(V_FLAG);
        } else {
            clearFlag(V_FLAG);
        }
    }

    private void updateSubFlags16(int operand1, int operand2, int fullResult) {
        int result16 = fullResult & 0xFFFF;

        // Z (Zero)
        if (result16 == 0) {
            setFlag(Z_FLAG);
        } else {
            clearFlag(Z_FLAG);
        }

        // N (Negative) - check bit 15
        if ((result16 & 0x8000) != 0) {
            setFlag(N_FLAG);
        } else {
            clearFlag(N_FLAG);
        }

        // C (Borrow) - set if borrow occurred (unsigned underflow)
        if (fullResult < 0) {
            setFlag(C_FLAG);
        } else {
            clearFlag(C_FLAG);
        }

        // V (Overflow) - signed overflow detection
        boolean op1Sign = (operand1 & 0x8000) != 0;
        boolean op2Sign = (operand2 & 0x8000) != 0;
        boolean resSign = (result16 & 0x8000) != 0;

        if (op1Sign != op2Sign && op1Sign != resSign) {
            setFlag(V_FLAG);
        } else {
            clearFlag(V_FLAG);
        }
    }

    // ADDD
    private void addd_immediate() {
        int operand1 = getD();
        int operand2 = fetchWord();
        int fullResult = operand1 + operand2;
        setD(fullResult & 0xFFFF);
        updateAddFlags16(operand1, operand2, fullResult);
    }
    private void addd_direct() {
        int ea = getDirectAdress();
        int operand1 = getD();
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 + operand2;

        setD(fullResult & 0xFFFF);
        updateAddFlags16(operand1, operand2, fullResult);
    }
    private void addd_extended() {
        int ea = getExtendAdress();
        int operand1 = getD();
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 + operand2;

        setD(fullResult & 0xFFFF);
        updateAddFlags16(operand1, operand2, fullResult);
    }
    private void addd_indexed() {
        int ea = getIndexedAddress();
        int operand1 = getD();
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 + operand2;

        setD(fullResult & 0xFFFF);
        updateAddFlags16(operand1, operand2, fullResult);
    }
    //SUBD
    private void subd_immediate() {
        int operand1 = getD();
        int operand2 = fetchWord();
        int fullResult = operand1 - operand2;

        setD(fullResult & 0xFFFF);
        updateSubFlags16(operand1, operand2, fullResult);
    }
    private void subd_direct() {
        int ea = getDirectAdress();
        int operand1 = getD();
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        setD(fullResult & 0xFFFF);
        updateSubFlags16(operand1, operand2, fullResult);
    }
    private void subd_extended() {
        int ea = getExtendAdress();
        int operand1 = getD();
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        setD(fullResult & 0xFFFF);
        updateSubFlags16(operand1, operand2, fullResult);
    }
    private void subd_indexed() {
        int ea = getIndexedAddress();
        int operand1 = getD();
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        setD(fullResult & 0xFFFF);
        updateSubFlags16(operand1, operand2, fullResult);
    }
    //CMPD
    // CMPD Immediate (Opcode: 0x1083)
    private void cmpd_immediate() {
        int operand1 = getD();
        int operand2 = fetchWord();
        int fullResult = operand1 - operand2;

        // Compare doesn't store result, only updates flags
        updateSubFlags16(operand1, operand2, fullResult);
    }

    // CMPD Direct (Opcode: 0x1093)
    private void cmpd_direct() {
        int ea = getDirectAdress();
        int operand1 = getD();
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }

    // CMPD Extended (Opcode: 0x10B3)
    private void cmpd_extended() {
        int ea = getExtendAdress();
        int operand1 = getD();
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }
    // CMPD INDEXED (Opcode: 0x10A3)
    private void cmpd_indexed() {
        int ea = getIndexedAddress();
        int operand1 = getD();
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }

    //CMPX
    // CMPX Immediate (Opcode: 0x8C)
    private void cmpx_immediate() {
        int operand1 = X;
        int operand2 = fetchWord();
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }

    // CMPX Direct (Opcode: 0x9C)
    private void cmpx_direct() {
        int ea = getDirectAdress();
        int operand1 = X;
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }

    // CMPX Extended (Opcode: 0xBC)
    private void cmpx_extended() {
        int ea = getExtendAdress();
        int operand1 = X;
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }
    // CMPX INDEXED (Opcode: 0xAC)
    private void cmpx_indexed() {
        int ea = getIndexedAddress();
        int operand1 = X;
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }
    //CMPY
    // CMPY Immediate (Opcode: 0x108C)
    private void cmpy_immediate() {
        int operand1 = Y;
        int operand2 = fetchWord();
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }

    // CMPY Direct (Opcode: 0x109C)
    private void cmpy_direct() {
        int ea = getDirectAdress();
        int operand1 = Y;
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }

    // CMPY Extended (Opcode: 0x10BC)
    private void cmpy_extended() {
        int ea = getExtendAdress();
        int operand1 = Y;
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }
    // CMPY INDEXED (Opcode: 0x10AC)
    private void cmpy_indexed() {
        int ea = getIndexedAddress();
        int operand1 = Y;
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }

    //CMPU
    // CMPU Immediate (Opcode: 0x1183)
    private void cmpu_immediate() {
        int operand1 = U;
        int operand2 = fetchWord();
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }

    // CMPU Direct (Opcode: 0x1193)
    private void cmpu_direct() {
        int ea = getDirectAdress();
        int operand1 = U;
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }

    // CMPU Extended (Opcode: 0x11B3)
    private void cmpu_extended() {
        int ea = getExtendAdress();
        int operand1 = U;
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }
    // CMPU INDEXED (Opcode: 0x11A3)
    private void cmpu_indexed() {
        int ea = getIndexedAddress();
        int operand1 = U;
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }
    //CMPS

    // CMPS Immediate (Opcode: 0x118C)
    private void cmps_immediate() {
        int operand1 = S;
        int operand2 = fetchWord();
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }

    // CMPS Direct (Opcode: 0x119C)
    private void cmps_direct() {
        int ea = getDirectAdress();
        int operand1 = S;
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }

    // CMPS Extended (Opcode: 0x11BC)
    private void cmps_extended() {
        int ea = getExtendAdress();
        int operand1 = S;
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }
    // CMPS INDEXED (Opcode: 0x11AC)
    private void cmps_indexed() {
        int ea = getIndexedAddress();
        int operand1 = S;
        int operand2 = ram.ReadWord(ea);
        int fullResult = operand1 - operand2;

        updateSubFlags16(operand1, operand2, fullResult);
    }
    //fct helper pur flag z ued in these ones
    private void updateZFlag16(int value) {
        if ((value & 0xFFFF) == 0) {
            setFlag(Z_FLAG);
        } else {
            clearFlag(Z_FLAG);
        }
    }








    //fct helper pour LEA flag z
    private void updateLEAFlags(int address) {
        if ((address & 0xFFFF) == 0) {
            setFlag(Z_FLAG);
        } else {
            clearFlag(Z_FLAG);
        }
    }

    private void setRegister(int regBits, int value) {
        switch (regBits) {
            case 0: X = value & 0xFFFF; break;
            case 1: Y = value & 0xFFFF; break;
            case 2: U = value & 0xFFFF; break;
            case 3: S = value & 0xFFFF; break;
        }
    }

    private int getIndexedAddress() {
        int postByte = fetchByte();

        // Bits 5-6 select the register (00=X, 01=Y, 10=U, 11=S)
        int regBits = (postByte >> 5) & 0x03;
        int register;

        switch (regBits) {
            case 0: register = X; break;
            case 1: register = Y; break;
            case 2: register = U; break;
            case 3: register = S; break;
            default: register = X; break;
        }

        // Check bit 7 to determine mode
        if ((postByte & 0x80) == 0) {
            // ===== 5-bit offset mode =====
            int offset = postByte & 0x1F;

            // Sign extend if bit 4 is set (negative)
            if ((offset & 0x10) != 0) {
                offset |= 0xFFFFFFE0;
            }

            return (register + offset) & 0xFFFF;
        } else {
            // ===== Extended indexed modes =====
            int mode = postByte & 0x0F;

            switch (mode) {
                case 0x00: // ,R+ (post-increment by 1)
                {
                    int ea = register;
                    setRegister(regBits, (register + 1) & 0xFFFF);
                    return ea;
                }

                case 0x01: // ,R++ (post-increment by 2)
                {
                    int ea = register;
                    setRegister(regBits, (register + 2) & 0xFFFF);
                    return ea;
                }

                case 0x02: // ,-R (pre-decrement by 1)
                {
                    int newReg = (register - 1) & 0xFFFF;
                    setRegister(regBits, newReg);
                    return newReg;
                }

                case 0x03: // ,--R (pre-decrement by 2)
                {
                    int newReg = (register - 2) & 0xFFFF;
                    setRegister(regBits, newReg);
                    return newReg;
                }

                case 0x04: // ,R (no offset)
                    return register;

                case 0x05: // B,R (B register offset)
                {
                    int offset = B;
                    if ((offset & 0x80) != 0) {
                        offset |= 0xFFFFFF00;
                    }
                    return (register + offset) & 0xFFFF;
                }

                case 0x06: // A,R (A register offset)
                {
                    int offset = A;
                    if ((offset & 0x80) != 0) {
                        offset |= 0xFFFFFF00;
                    }
                    return (register + offset) & 0xFFFF;
                }

                case 0x08: // 8-bit offset,R
                {
                    int offset = fetchSignedByte();
                    return (register + offset) & 0xFFFF;
                }

                case 0x09: // 16-bit offset,R
                {
                    int offset = fetchSignedWord();
                    return (register + offset) & 0xFFFF;
                }

                case 0x0B: // D,R (D register offset)
                {
                    int offset = getD();
                    if ((offset & 0x8000) != 0) {
                        offset |= 0xFFFF0000;
                    }
                    return (register + offset) & 0xFFFF;
                }

                case 0x0C: // 8-bit offset,PC (PC-relative)
                {
                    int offset = fetchSignedByte();
                    return (PC + offset) & 0xFFFF;
                }

                case 0x0D: // 16-bit offset,PC (PC-relative)
                {
                    int offset = fetchSignedWord();
                    return (PC + offset) & 0xFFFF;
                }

                case 0x0F: // [Extended indirect]
                {
                    int address = fetchWord();
                    return ram.ReadWord(address);
                }

                // Indirect modes (with brackets [])
                case 0x11: // [,R++]
                {
                    int address = register;
                    setRegister(regBits, (register + 2) & 0xFFFF);
                    return ram.ReadWord(address);
                }

                case 0x13: // [,--R]
                {
                    int newReg = (register - 2) & 0xFFFF;
                    setRegister(regBits, newReg);
                    return ram.ReadWord(newReg);
                }

                case 0x14: // [,R]
                    return ram.ReadWord(register);

                case 0x15: // [B,R]
                {
                    int offset = B;
                    if ((offset & 0x80) != 0) {
                        offset |= 0xFFFFFF00;
                    }
                    int address = (register + offset) & 0xFFFF;
                    return ram.ReadWord(address);
                }

                case 0x16: // [A,R]
                {
                    int offset = A;
                    if ((offset & 0x80) != 0) {
                        offset |= 0xFFFFFF00;
                    }
                    int address = (register + offset) & 0xFFFF;
                    return ram.ReadWord(address);
                }

                case 0x18: // [8-bit offset,R]
                {
                    int offset = fetchSignedByte();
                    int address = (register + offset) & 0xFFFF;
                    return ram.ReadWord(address);
                }

                case 0x19: // [16-bit offset,R]
                {
                    int offset = fetchSignedWord();
                    int address = (register + offset) & 0xFFFF;
                    return ram.ReadWord(address);
                }

                case 0x1B: // [D,R]
                {
                    int offset = getD();
                    if ((offset & 0x8000) != 0) {
                        offset |= 0xFFFF0000;
                    }
                    int address = (register + offset) & 0xFFFF;
                    return ram.ReadWord(address);
                }

                case 0x1C: // [8-bit offset,PC]
                {
                    int offset = fetchSignedByte();
                    int address = (PC + offset) & 0xFFFF;
                    return ram.ReadWord(address);
                }

                case 0x1D: // [16-bit offset,PC]
                {
                    int offset = fetchSignedWord();
                    int address = (PC + offset) & 0xFFFF;
                    return ram.ReadWord(address);
                }

                default:
                    throw new IllegalStateException("Invalid indexed addressing mode: 0x" +
                            Integer.toHexString(postByte));
            }
        }
    }


    private void leax_indexed() {
        X = getIndexedAddress();
        updateLEAFlags(X);
    }

    private void leay_indexed() {
        Y = getIndexedAddress();
        updateLEAFlags(Y);
    }

    private void leau_indexed() {
        U = getIndexedAddress();
        updateLEAFlags(U);
    }

    private void leas_indexed() {
        S = getIndexedAddress();
        updateLEAFlags(S);
    }














    //BRANCHE AND JUMP
       // Fetch a signed byte for relative addressing (branches).
       // Converts 8-bit signed value to signed int.

    private int fetchSignedByte() {
        int value = fetchByte();
         // Convert to signed: if bit 7 is set, it's negative
        if ((value & 0x80) != 0) {
            return value | 0xFFFFFF00;  // Sign extend
        }
        return value;
    }


     // Fetch a signed word for long relative addressing.
     // Converts 16-bit signed value to signed int.
    private int fetchSignedWord() {
        int value = fetchWord();
        // Convert to signed: if bit 15 is set, it's negative
        if ((value & 0x8000) != 0) {
            return value | 0xFFFF0000;  // Sign extend
        }
        return value;
    }
    // Push a byte onto the stack (S register).
    private void pushByte(int value) {
        S = (S - 1) & 0xFFFF;
        ram.WriteInMemory(S, value & 0xFF);
    }

    //  Push a word onto the stack (S register).
      //High byte is pushed first, then low byte.

    private void pushWord(int value) {
        pushByte(value & 0xFF);         // Low byte first
        pushByte((value >> 8) & 0xFF);  // High byte second
    }


    //  Pull a byte from the stack (S register).

    private int pullByte() {
        int value = ram.ReadFromMemory(S);
        S = (S + 1) & 0xFFFF;
        return value;
    }
     //Pull a word from the stack (S register).
     // High byte is pulled first, then low byte.

    private int pullWord() {
        int high = pullByte();  // High byte first
        int low = pullByte();   // Low byte second
        return (high << 8) | low;
    }


    // JMP Extended (Opcode: 0x7E)
    private void jmp_extended() {
        PC = getExtendAdress();
    }

    // JMP Direct (Opcode: 0x0E)
    private void jmp_direct() {
        PC = getDirectAdress();
    }
    // JMP INDEXED (Opcode: 0x6E)
    private void jmp_indexed() {
        // Gets the address from [5,X], [,Y++], etc.
        int ea = getIndexedAddress();
        // Sets the PC to that calculated address
        PC = ea;
    }

//  JSR (Jump to Subroutine)
// Pushes return address onto stack, then jumps

    // JSR Extended (Opcode: 0xBD)
    private void jsr_extended() {
        int targetAddress = getExtendAdress();
        pushWord(PC);  // Save return address
        PC = targetAddress;
    }

    // JSR Direct (Opcode: 0x9D)
    private void jsr_direct() {
        int targetAddress = getDirectAdress();
        pushWord(PC);  // Save return address
        PC = targetAddress;
    }
    // JSR INDEXED (Opcode: 0xAD)
    private void jsr_indexed() {
        // Gets the address from [5,X], [,Y++], etc.
        int targetAddress = getIndexedAddress();

        // Same as your other JSR functions
        pushWord(PC);  // Save return address
        PC = targetAddress;
    }

    // RTS (Return from Subroutine)
// Opcode: 0x39 (Inherent)
    private void rts() {
        PC = pullWord();  // Restore return address from stack
    }


    //  BRA (Branch Always)
// Opcode: 0x20 (Relative)
    private void bra() {
        int offset = fetchSignedByte();
        PC = (PC + offset) & 0xFFFF;
    }

    //  BRN (Branch Never)
// Opcode: 0x21 (Relative) - Does nothing, just skips offset
    private void brn() {
        fetchSignedByte();  // Fetch offset but don't use it
        // PC stays the same
    }

    //  BEQ (Branch if Equal)
// Opcode: 0x27 (Relative) - Branch if Z flag is set
    private void beq() {
        int offset = fetchSignedByte();
        if (isFlagSet(Z_FLAG)) {
            PC = (PC + offset) & 0xFFFF;
        }
    }

    //  BNE (Branch if Not Equal)
// Opcode: 0x26 (Relative) - Branch if Z flag is clear
    private void bne() {
        int offset = fetchSignedByte();
        if (!isFlagSet(Z_FLAG)) {
            PC = (PC + offset) & 0xFFFF;
        }
    }

    //  BCC (Branch if Carry Clear)
// Opcode: 0x24 (Relative) - Branch if C flag is clear
    private void bcc() {
        int offset = fetchSignedByte();
        if (!isFlagSet(C_FLAG)) {
            PC = (PC + offset) & 0xFFFF;
        }
    }

    // BHS (Branch if Higher or Same) - Same as BCC
// Opcode: 0x24 (Alias for BCC)
    private void bhs() {
        bcc();  // Same instruction
    }

    //  BCS (Branch if Carry Set)
// Opcode: 0x25 (Relative) - Branch if C flag is set
    private void bcs() {
        int offset = fetchSignedByte();
        if (isFlagSet(C_FLAG)) {
            PC = (PC + offset) & 0xFFFF;
        }
    }

    // BLO (Branch if Lower) - Same as BCS
// Opcode: 0x25 (Alias for BCS)
    private void blo() {
        bcs();  // Same instruction
    }

    //  BMI (Branch if Minus)
// Opcode: 0x2B (Relative) - Branch if N flag is set
    private void bmi() {
        int offset = fetchSignedByte();
        if (isFlagSet(N_FLAG)) {
            PC = (PC + offset) & 0xFFFF;
        }
    }

    //  BPL (Branch if Plus)
// Opcode: 0x2A (Relative) - Branch if N flag is clear
    private void bpl() {
        int offset = fetchSignedByte();
        if (!isFlagSet(N_FLAG)) {
            PC = (PC + offset) & 0xFFFF;
        }
    }

    //  BVS (Branch if Overflow Set)
// Opcode: 0x29 (Relative) - Branch if V flag is set
    private void bvs() {
        int offset = fetchSignedByte();
        if (isFlagSet(V_FLAG)) {
            PC = (PC + offset) & 0xFFFF;
        }
    }

    //  BVC (Branch if Overflow Clear)
// Opcode: 0x28 (Relative) - Branch if V flag is clear
    private void bvc() {
        int offset = fetchSignedByte();
        if (!isFlagSet(V_FLAG)) {
            PC = (PC + offset) & 0xFFFF;
        }
    }

    //  BHI (Branch if Higher)
// Opcode: 0x22 (Relative) - Branch if C=0 AND Z=0 (unsigned >)
    private void bhi() {
        int offset = fetchSignedByte();
        if (!isFlagSet(C_FLAG) && !isFlagSet(Z_FLAG)) {
            PC = (PC + offset) & 0xFFFF;
        }
    }

    //  BLS (Branch if Lower or Same)
// Opcode: 0x23 (Relative) - Branch if C=1 OR Z=1 (unsigned <=)
    private void bls() {
        int offset = fetchSignedByte();
        if (isFlagSet(C_FLAG) || isFlagSet(Z_FLAG)) {
            PC = (PC + offset) & 0xFFFF;
        }
    }

    //  BGT (Branch if Greater Than)
// Opcode: 0x2E (Relative) - Branch if Z=0 AND (N XOR V)=0 (signed >)
    private void bgt() {
        int offset = fetchSignedByte();
        boolean nSet = isFlagSet(N_FLAG);
        boolean vSet = isFlagSet(V_FLAG);
        if (!isFlagSet(Z_FLAG) && (nSet == vSet)) {
            PC = (PC + offset) & 0xFFFF;
        }
    }

    //  BGE (Branch if Greater or Equal)
// Opcode: 0x2C (Relative) - Branch if (N XOR V)=0 (signed >=)
    private void bge() {
        int offset = fetchSignedByte();
        boolean nSet = isFlagSet(N_FLAG);
        boolean vSet = isFlagSet(V_FLAG);
        if (nSet == vSet) {
            PC = (PC + offset) & 0xFFFF;
        }
    }

    //  BLE (Branch if Less or Equal)
// Opcode: 0x2F (Relative) - Branch if Z=1 OR (N XOR V)=1 (signed <=)
    private void ble() {
        int offset = fetchSignedByte();
        boolean nSet = isFlagSet(N_FLAG);
        boolean vSet = isFlagSet(V_FLAG);
        if (isFlagSet(Z_FLAG) || (nSet != vSet)) {
            PC = (PC + offset) & 0xFFFF;
        }
    }

    //  BLT (Branch if Less Than)
// Opcode: 0x2D (Relative) - Branch if (N XOR V)=1 (signed <)
    private void blt() {
        int offset = fetchSignedByte();
        boolean nSet = isFlagSet(N_FLAG);
        boolean vSet = isFlagSet(V_FLAG);
        if (nSet != vSet) {
            PC = (PC + offset) & 0xFFFF;
        }
    }


// LONG BRANCH INSTRUCTIONS (16-bit offset)


    //  LBRA (Long Branch Always)
// Opcode: 0x16 (Long Relative)
    private void lbra() {
        int offset = fetchSignedWord();
        PC = (PC + offset) & 0xFFFF;
    }

    //  LBEQ (Long Branch if Equal)
// Opcode: 0x1027 (Long Relative)
    private void lbeq() {
        int offset = fetchSignedWord();
        if (isFlagSet(Z_FLAG)) {
            PC = (PC + offset) & 0xFFFF;
        }
    }

    //  LBNE (Long Branch if Not Equal)
// Opcode: 0x1026 (Long Relative)
    private void lbne() {
        int offset = fetchSignedWord();
        if (!isFlagSet(Z_FLAG)) {
            PC = (PC + offset) & 0xFFFF;
        }
    }


// BSR (Branch to Subroutine)

    //  BSR (Branch to Subroutine)
// Opcode: 0x8D (Relative) - Like JSR but uses relative addressing
    private void bsr() {
        int offset = fetchSignedByte();
        pushWord(PC);  // Save return address
        PC = (PC + offset) & 0xFFFF;
    }

    //  LBSR (Long Branch to Subroutine)
// Opcode: 0x17 (Long Relative)
    private void lbsr() {
        int offset = fetchSignedWord();
        pushWord(PC);  // Save return address
        PC = (PC + offset) & 0xFFFF;
    }







    // function that print all the registers i mean get
    public void printAllRegisters() {
        System.out.printf("PC: %04X  D: %04X (A:%02X B:%02X)  X: %04X  Y: %04X  U: %04X  S: %04X  DP: %02X\n",
                PC, getD(), A, B, X, Y, U, S, DP);
        System.out.printf("CC: %02X (E F H I N Z V C)\n", CC);
        System.out.printf("    %d %d %d %d %d %d %d %d\n",
                (CC & E_FLAG) >> 7, (CC & F_FLAG) >> 6, (CC & H_FLAG) >> 5, (CC & I_FLAG) >> 4,
                (CC & N_FLAG) >> 3, (CC & Z_FLAG) >> 2, (CC & V_FLAG) >> 1, (CC & C_FLAG));
    }


    // the big switch
    public void step() {
        // 1. Fetch the opcode from the Program Counter
        int opcode = fetchByte();

        // 2. Handle 2-byte opcodes (Page 1 and Page 2)
        if (opcode == 0x10) {
            // It's a "Page 1" instruction, fetch the *real* opcode
            opcode = 0x1000 | fetchByte();
        } else if (opcode == 0x11) {
            // It's a "Page 2" instruction
            opcode = 0x1100 | fetchByte();
        }

        // 3. Decode and execute the opcode
        // This switch statement calls all your hard work.
        switch (opcode) {
            // --- 16-bit LOAD/STORE ( Part 2) ---
            case 0xCC:
                ldd_immediate();
                break;
            case 0xDC:
                ldd_direct();
                break;
            case 0xFC:
                ldd_extend();
                break;
            case 0xDD:
                std_direct();
                break;
            case 0xFD:
                std_extend();
                break;

            case 0x8E:
                ldx_immediate();
                break;
            case 0x9E:
                ldx_direct();
                break;
            case 0xBE:
                ldx_extend();
                break;
            case 0x9F:
                stx_direct();
                break;
            case 0xBF:
                stx_extend();
                break;

            case 0x108E:
                ldy_immediate();
                break;
            case 0x109E:
                ldy_direct();
                break;
            case 0x10BE:
                ldy_extend();
                break;
            case 0x109F:
                sty_direct();
                break;
            case 0x10BF:
                sty_extend();
                break;

            case 0xCE:
                ldu_immediate();
                break;
            case 0xDE:
                ldu_direct();
                break;
            case 0xFE:
                ldu_extend();
                break;
            case 0xDF:
                stu_direct();
                break;
            case 0xFF:
                stu_extend();
                break;

            case 0x10CE:
                lds_immediate();
                break;
            case 0x10DE:
                lds_direct();
                break;
            case 0x10FE:
                lds_extend();
                break;
            case 0x10DF:
                sts_direct();
                break;
            case 0x10FF:
                sts_extend();
                break;
            case 0xEC: ldd_indexed(); break;
            case 0xED: std_indexed(); break;
            case 0xAE: ldx_indexed(); break;
            case 0xAF: stx_indexed(); break;
            case 0x10AE: ldy_indexed(); break;
            case 0x10AF: sty_indexed(); break;
            case 0xEE: ldu_indexed(); break;
            case 0xEF: stu_indexed(); break;
            case 0x10EE: lds_indexed(); break;
            case 0x10EF: sts_indexed(); break;

            // --- 16-bit ARITHMETIC ( Part 4) ---
            case 0xC3:
                addd_immediate();
                break;
            case 0xD3:
                addd_direct();
                break;
            case 0xF3:
                addd_extended();
                break;
            case 0x83:
                subd_immediate();
                break;
            case 0x93:
                subd_direct();
                break;
            case 0xB3:
                subd_extended();
                break;

            case 0x1083:
                cmpd_immediate();
                break;
            case 0x1093:
                cmpd_direct();
                break;
            case 0x10B3:
                cmpd_extended();
                break;
            case 0x8C:
                cmpx_immediate();
                break;
            case 0x9C:
                cmpx_direct();
                break;
            case 0xBC:
                cmpx_extended();
                break;
            // ... (add other CMPY, CMPU, CMPS opcodes here) ...

            case 0x30:
                leax_indexed();
                break;
            case 0x31:
                leay_indexed();
                break;
            case 0x32:
                leas_indexed();
                break;
            case 0x33:
                leau_indexed();
                break;
            case 0xE3: addd_indexed(); break;
            case 0xA3: subd_indexed(); break;
            case 0x10A3: cmpd_indexed(); break;
            case 0xAC: cmpx_indexed(); break;
            case 0x10AC: cmpy_indexed(); break;
            case 0x11A3: cmpu_indexed(); break;
            case 0x11AC: cmps_indexed(); break;

            // ... (add INX, DECX, etc. opcodes here) ...

            // --- BRANCH/JUMP ( Part 6) ---
            case 0x20:
                bra();
                break;
            case 0x21:
                brn();
                break;
            case 0x26:
                bne();
                break;
            case 0x27:
                beq();
                break;
            case 0x24:
                bcc();
                break; // or BHS
            case 0x25:
                bcs();
                break; // or BLO
            case 0x2A:
                bpl();
                break;
            case 0x2B:
                bmi();
                break;
            case 0x2C:
                bge();
                break;
            case 0x2D:
                blt();
                break;
            case 0x2E:
                bgt();
                break;
            case 0x2F:
                ble();
                break;
            case 0x22:
                bhi();
                break;
            case 0x23:
                bls();
                break;
            case 0x28:
                bvc();
                break;
            case 0x29:
                bvs();
                break;

            case 0x16:
                lbra();
                break;
            case 0x8D:
                bsr();
                break;
            case 0x17:
                lbsr();
                break;

            case 0x0E:
                jmp_direct();
                break;
            case 0x7E:
                jmp_extended();
                break;
            case 0x6E: jmp_indexed();  break;
            case 0x9D:
                jsr_direct();
                break;
            case 0xBD:
                jsr_extended();
                break;
            case 0xAD: jsr_indexed();  break;
            case 0x39:
                rts();
                break;
            default:
                // We need to 'undo' the fetchByte if we don't know the opcode
                PC = (PC - 1) & 0xFFFF;
                throw new IllegalStateException(String.format("Unhandled opcode: 0x%02X at address 0x%04X",
                        ram.ReadFromMemory(PC), PC));
        }
    }





        public static void loadTestProgram(RAM ram) {
        int[] program = {
                // C000: LDD #$1234
                0xCC, 0x12, 0x34,
                // C003: STD $0100
                0xFD, 0x01, 0x00,
                // C006: LDX #$0001
                0x8E, 0x00, 0x01,
                // C009: ADDD $0100 (Extended)
                // (D = $1234 + $1234 = $2468)
                0xF3, 0x01, 0x00,
                // C00C: CMPX #$0002
                // (Compare X=$0001 with $0002. Z flag will be 0)
                0x8C, 0x00, 0x02,
                // C00F: BNE SUCCESS (Branch if Not Equal, Z=0)
                // Branch to C014. Offset is +3 (0x03)
                0x26, 0x03,
                // C011: LDU #$DEAD (FAIL_PATH)
                0xCE, 0xDE, 0xAD,
                // C014: SUCCESS: LDY #$AAAA
                // (2-byte opcode 0x10)
                0x10, 0x8E, 0xAA, 0xAA,
                // C018: BRA HALT
                // Branch to C01A. Offset is 0 (0x00)
                // 0x20, 0x00, -> This is wrong. C01A - (C018+2) = 0.
                0x20, 0x00,
                // C01A: HALT: JMP HALT
                // (Jumps to itself, $C01A)
                0x7E, 0xC0, 0x1A
        };
        // Write the program bytes to RAM starting at 0xC000
        for (int i = 0; i < program.length; i++) {
            ram.WriteInMemory(0xC000 + i, program[i]);
        }
    }

    // hady fct pour verifier les resultats

    public static void verifyResults(CPU cpu) {
        // We check the registers for the expected final values
        boolean d_ok = cpu.getA() == 0x24 && cpu.getB() == 0x68; // D = $2468
        boolean x_ok = cpu.getX() == 0x0001;
        boolean y_ok = cpu.getY() == 0xAAAA; // BNE was successful
        boolean u_ok = cpu.getU() == 0x0000; // LDU #$DEAD was skipped

        if (d_ok && x_ok && y_ok && u_ok) {
            System.out.println(" SUCCESS! All registers match.");
        } else {
            System.out.println(" FAILURE!");
            if (!d_ok) System.out.println("   D Register should be $2468");
            if (!x_ok) System.out.println("   X Register should be $0001");
            if (!y_ok) System.out.println("   Y Register should be $AAAA (BNE test failed)");
            if (!u_ok) System.out.println("   U Register should be $0000 (BNE test failed)");
        }
    }
























}