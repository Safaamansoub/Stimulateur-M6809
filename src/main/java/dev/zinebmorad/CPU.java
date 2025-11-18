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
        A = 0; B = 0; X = 0; Y = 0; U = 0; S = 0; DP = 0; CC = 0;
        PC = 0x0000; // Simple reset for simulation purposes

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

    // 8 bits LOAD ANS STORE
    private void updateZNFlags8(int value) {
        int v = value & 0xFF;
        if (v == 0) setFlag(Z_FLAG);
        else clearFlag(Z_FLAG);
        if ((v & 0x80) != 0) setFlag(N_FLAG);
        else clearFlag(N_FLAG);
        clearFlag(V_FLAG);
    }

    // ========== LDA ==========
    private void LDA_IMM() { // Immediate
        A = fetchByte() & 0xFF;
        updateZNFlags8(A);
    }

    private void LDA_DIR() { // Direct
        int ea = getDirectAdress();
        A = ram.ReadFromMemory(ea) & 0xFF;
        updateZNFlags8(A);
    }

    private void LDA_EXT() { // Extended
        int ea = getExtendAdress();
        A = ram.ReadFromMemory(ea) & 0xFF;
        updateZNFlags8(A);
    }

    private void LDA_IND() {
        int addr = getIndexedAddress();
        A = ram.ReadFromMemory(addr);
        updateZNFlags8(A);
    }

    private void LDB_IMM() {
        B = fetchByte() & 0xFF;
        updateZNFlags8(B);
    }

    private void LDB_DIR() {
        int ea = getDirectAdress();
        B = ram.ReadFromMemory(ea) & 0xFF;
        updateZNFlags8(B);
    }

    private void LDB_EXT() {
        int ea = getExtendAdress();
        B = ram.ReadFromMemory(ea) & 0xFF;
        updateZNFlags8(B);
    }

    private void LDB_IND() {
        int addr = getIndexedAddress();
        B = ram.ReadFromMemory(addr);
        updateZNFlags8(B);
    }

    // ========== STA ==========
    private void STA_DIR() {
        int ea = getDirectAdress();
        ram.WriteInMemory(ea, A & 0xFF);
    }

    private void STA_EXT() {
        int ea = getExtendAdress();
        ram.WriteInMemory(ea, A & 0xFF);
    }

    private void STA_IND() {
        int addr = getIndexedAddress();
        ram.WriteInMemory(addr, A);
    }

    // ========== STB ==========
    private void STB_DIR() {
        int ea = getDirectAdress();
        ram.WriteInMemory(ea, B & 0xFF);
    }

    private void STB_EXT() {
        int ea = getExtendAdress();
        ram.WriteInMemory(ea, B & 0xFF);
    }

    private void STB_IND() {
        int addr = getIndexedAddress();
        ram.WriteInMemory(addr, B);

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









    // ===============================
    // 8-BIT ARITHMETIC INSTRUCTIONS
    // ===============================
    private void updateAddFlags8(int op1, int op2, int result) {
        int r = result & 0xFF;
        if (((op1 ^ op2 ^ r) & 0x10) != 0) setFlag(H_FLAG);
        else clearFlag(H_FLAG);
        if (result > 0xFF) setFlag(C_FLAG);
        else clearFlag(C_FLAG);
        if (((op1 ^ result) & (op2 ^ result) & 0x80) != 0) setFlag(V_FLAG);
        else clearFlag(V_FLAG);
        if ((r & 0x80) != 0) setFlag(N_FLAG);
        else clearFlag(N_FLAG);
        if (r == 0) setFlag(Z_FLAG);
        else clearFlag(Z_FLAG);
    }

    private void updateSubFlags8(int op1, int op2, int result) {
        int r = result & 0xFF;
        if (result < 0) setFlag(C_FLAG);
        else clearFlag(C_FLAG);
        if (((op1 ^ op2) & (op1 ^ result) & 0x80) != 0) setFlag(V_FLAG);
        else clearFlag(V_FLAG);
        if ((r & 0x80) != 0) setFlag(N_FLAG);
        else clearFlag(N_FLAG);
        if (r == 0) setFlag(Z_FLAG);
        else clearFlag(Z_FLAG);
    }

    // --- ADDA Immediate ---
    private void ADDA_IMM() {
        int op1 = A;
        int op2 = fetchByte();
        int result = op1 + op2;
        A = result & 0xFF;
        updateAddFlags8(op1, op2, result);
    }

    // --- ADDA Direct ---
    private void ADDA_DIR() {
        int addr = getDirectAdress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 + op2;
        A = result & 0xFF;
        updateAddFlags8(op1, op2, result);
    }

    // --- ADDA Extended ---
    private void ADDA_EXT() {
        int addr = getExtendAdress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 + op2;
        A = result & 0xFF;
        updateAddFlags8(op1, op2, result);
    }
    private void ADDA_IND() {
        int addr = getIndexedAddress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 + op2;
        A = result & 0xFF;
        updateAddFlags8(op1, op2, result);
    }
    private void ADDB_IMM() {
        int op1 = B;
        int op2 = fetchByte();
        int result = op1 + op2;
        B = result & 0xFF;
        updateAddFlags8(op1, op2, result);
    }

    private void ADDB_DIR() {
        int addr = getDirectAdress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 + op2;
        B = result & 0xFF;
        updateAddFlags8(op1, op2, result);
    }

    private void ADDB_EXT() {
        int addr = getExtendAdress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 + op2;
        B = result & 0xFF;
        updateAddFlags8(op1, op2, result);
    }

    private void ADDB_IND() {
        int addr = getIndexedAddress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 + op2;
        B = result & 0xFF;
        updateAddFlags8(op1, op2, result);
    }
    private void ADCA_IMM() {
        int op1 = A;
        int op2 = fetchByte();
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 + op2 + carry;
        A = result & 0xFF;
        updateAddFlags8(op1, op2 + carry, result);
    }

    private void ADCA_DIR() {
        int addr = getDirectAdress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 + op2 + carry;
        A = result & 0xFF;
        updateAddFlags8(op1, op2 + carry, result);
    }

    private void ADCA_EXT() {
        int addr = getExtendAdress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 + op2 + carry;
        A = result & 0xFF;
        updateAddFlags8(op1, op2 + carry, result);
    }

    private void ADCA_IND() {
        int addr = getIndexedAddress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 + op2 + carry;
        A = result & 0xFF;
        updateAddFlags8(op1, op2 + carry, result);
    }

    // IMM
    private void ADCB_IMM() {
        int op1 = B;
        int op2 = fetchByte();
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 + op2 + carry;
        B = result & 0xFF;
        updateAddFlags8(op1, op2 + carry, result);
    }

    // DIR
    private void ADCB_DIR() {
        int addr = getDirectAdress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 + op2 + carry;
        B = result & 0xFF;
        updateAddFlags8(op1, op2 + carry, result);
    }

    // EXT
    private void ADCB_EXT() {
        int addr = getExtendAdress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 + op2 + carry;
        B = result & 0xFF;
        updateAddFlags8(op1, op2 + carry, result);
    }

    // IND
    private void ADCB_IND() {
        int addr = getIndexedAddress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 + op2 + carry;
        B = result & 0xFF;
        updateAddFlags8(op1, op2 + carry, result);
    }

    // Immediate
    private void SUBA_IMM() {
        int op1 = A;
        int op2 = fetchByte();
        int result = op1 - op2;
        A = result & 0xFF;
        updateSubFlags8(op1, op2, result);
    }

    // Direct
    private void SUBA_DIR() {
        int addr = getDirectAdress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 - op2;
        A = result & 0xFF;
        updateSubFlags8(op1, op2, result);
    }

    // Extended
    private void SUBA_EXT() {
        int addr = getExtendAdress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 - op2;
        A = result & 0xFF;
        updateSubFlags8(op1, op2, result);
    }

    // Indexed
    private void SUBA_IND() {
        int addr = getIndexedAddress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 - op2;
        A = result & 0xFF;
        updateSubFlags8(op1, op2, result);
    }

    private void SUBB_IMM() {
        int op1 = B;
        int op2 = fetchByte();
        int result = op1 - op2;
        B = result & 0xFF;
        updateSubFlags8(op1, op2, result);
    }

    private void SUBB_DIR() {
        int addr = getDirectAdress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 - op2;
        B = result & 0xFF;
        updateSubFlags8(op1, op2, result);
    }

    private void SUBB_EXT() {
        int addr = getExtendAdress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 - op2;
        B = result & 0xFF;
        updateSubFlags8(op1, op2, result);
    }

    private void SUBB_IND() {
        int addr = getIndexedAddress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 - op2;
        B = result & 0xFF;
        updateSubFlags8(op1, op2, result);
    }

    // IMM
    private void SBCA_IMM() {
        int op1 = A;
        int op2 = fetchByte();
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 - op2 - carry;
        A = result & 0xFF;
        updateSubFlags8(op1, op2 + carry, result);
    }

    // DIR
    private void SBCA_DIR() {
        int addr = getDirectAdress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 - op2 - carry;
        A = result & 0xFF;
        updateSubFlags8(op1, op2 + carry, result);
    }

    // EXT
    private void SBCA_EXT() {
        int addr = getExtendAdress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 - op2 - carry;
        A = result & 0xFF;
        updateSubFlags8(op1, op2 + carry, result);
    }

    // IND
    private void SBCA_IND() {
        int addr = getIndexedAddress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 - op2 - carry;
        A = result & 0xFF;
        updateSubFlags8(op1, op2 + carry, result);
    }

    // IMM
    private void SBCB_IMM() {
        int op1 = B;
        int op2 = fetchByte();
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 - op2 - carry;
        B = result & 0xFF;
        updateSubFlags8(op1, op2 + carry, result);
    }

    // DIR
    private void SBCB_DIR() {
        int addr = getDirectAdress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 - op2 - carry;
        B = result & 0xFF;
        updateSubFlags8(op1, op2 + carry, result);
    }

    // EXT
    private void SBCB_EXT() {
        int addr = getExtendAdress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 - op2 - carry;
        B = result & 0xFF;
        updateSubFlags8(op1, op2 + carry, result);
    }

    // IND
    private void SBCB_IND() {
        int addr = getIndexedAddress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int carry = isFlagSet(C_FLAG) ? 1 : 0;
        int result = op1 - op2 - carry;
        B = result & 0xFF;
        updateSubFlags8(op1, op2 + carry, result);
    }

    // IMM
    private void CMPA_IMM() {
        int op1 = A;
        int op2 = fetchByte();
        int result = op1 - op2;
        updateSubFlags8(op1, op2, result);
    }

    // DIR
    private void CMPA_DIR() {
        int addr = getDirectAdress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 - op2;
        updateSubFlags8(op1, op2, result);
    }

    // EXT
    private void CMPA_EXT() {
        int addr = getExtendAdress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 - op2;
        updateSubFlags8(op1, op2, result);
    }

    // IND
    private void CMPA_IND() {
        int addr = getIndexedAddress();
        int op1 = A;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 - op2;
        updateSubFlags8(op1, op2, result);
    }

    // IMM
    private void CMPB_IMM() {
        int op1 = B;
        int op2 = fetchByte();
        int result = op1 - op2;
        updateSubFlags8(op1, op2, result);
    }

    // DIR
    private void CMPB_DIR() {
        int addr = getDirectAdress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 - op2;
        updateSubFlags8(op1, op2, result);
    }

    // EXT
    private void CMPB_EXT() {
        int addr = getExtendAdress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 - op2;
        updateSubFlags8(op1, op2, result);
    }

    // IND
    private void CMPB_IND() {
        int addr = getIndexedAddress();
        int op1 = B;
        int op2 = ram.ReadFromMemory(addr);
        int result = op1 - op2;
        updateSubFlags8(op1, op2, result);
    }

    // INCA : incrément A
    private void INCA() {
        int before = A & 0xFF;
        int after = (before + 1) & 0xFF;
        A = after;
        if (after == 0) setFlag(Z_FLAG); else clearFlag(Z_FLAG);
        if ((after & 0x80) != 0) setFlag(N_FLAG); else clearFlag(N_FLAG);
        if (before == 0x7F) setFlag(V_FLAG); else clearFlag(V_FLAG);
    }

    // INCB : incrément B
    private void INCB() {
        int before = B & 0xFF;
        int after = (before + 1) & 0xFF;
        B = after;
        if (after == 0) setFlag(Z_FLAG); else clearFlag(Z_FLAG);
        if ((after & 0x80) != 0) setFlag(N_FLAG); else clearFlag(N_FLAG);
        if (before == 0x7F) setFlag(V_FLAG); else clearFlag(V_FLAG);
    }

    // DECA : décrément A
    private void DECA() {
        int before = A & 0xFF;
        int after = (before - 1) & 0xFF;
        A = after;
        if (after == 0) setFlag(Z_FLAG); else clearFlag(Z_FLAG);
        if ((after & 0x80) != 0) setFlag(N_FLAG); else clearFlag(N_FLAG);
        if (before == 0x80) setFlag(V_FLAG); else clearFlag(V_FLAG);
    }

    // DECB : décrément B
    private void DECB() {
        int before = B & 0xFF;
        int after = (before - 1) & 0xFF;
        B = after;
        if (after == 0) setFlag(Z_FLAG); else clearFlag(Z_FLAG);
        if ((after & 0x80) != 0) setFlag(N_FLAG); else clearFlag(N_FLAG);
        if (before == 0x80) setFlag(V_FLAG); else clearFlag(V_FLAG);
    }

    // CLRA : clear A
    private void CLRA() {
        A = 0;
        setFlag(Z_FLAG);
        clearFlag(N_FLAG);
        clearFlag(V_FLAG);
        clearFlag(C_FLAG);
    }

    // CLRB : clear B
    private void CLRB() {
        B = 0;
        setFlag(Z_FLAG);
        clearFlag(N_FLAG);
        clearFlag(V_FLAG);
        clearFlag(C_FLAG);
    }

    // TSTA : test A
    private void TSTA() {
        if ((A & 0x80) != 0) setFlag(N_FLAG); else clearFlag(N_FLAG);
        if (A == 0) setFlag(Z_FLAG); else clearFlag(Z_FLAG);
        clearFlag(V_FLAG);
    }

    // TSTB : test B
    private void TSTB() {
        if ((B & 0x80) != 0) setFlag(N_FLAG); else clearFlag(N_FLAG);
        if (B == 0) setFlag(Z_FLAG); else clearFlag(Z_FLAG);
        clearFlag(V_FLAG);
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
















    // Logic and Shift
    // ANDA
// IMM
    private void ANDA_IMM() {
        A = (A & fetchByte()) & 0xFF;
        updateZNFlags8(A);
    }
    // DIR
    private void ANDA_DIR() {
        int addr = getDirectAdress();
        A = (A & ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(A);
    }
    // EXT
    private void ANDA_EXT() {
        int addr = getExtendAdress();
        A = (A & ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(A);
    }
    // IND
    private void ANDA_IND() {
        int addr = getIndexedAddress();
        A = (A & ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(A);
    }

    // ANDB
// IMM
    private void ANDB_IMM() {
        B = (B & fetchByte()) & 0xFF;
        updateZNFlags8(B);
    }
    // DIR
    private void ANDB_DIR() {
        int addr = getDirectAdress();
        B = (B & ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(B);
    }
    // EXT
    private void ANDB_EXT() {
        int addr = getExtendAdress();
        B = (B & ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(B);
    }
    // IND
    private void ANDB_IND() {
        int addr = getIndexedAddress();
        B = (B & ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(B);
    }

    // ORA
    private void ORA_IMM() {
        A = (A | fetchByte()) & 0xFF;
        updateZNFlags8(A);
    }
    private void ORA_DIR() {
        int addr = getDirectAdress();
        A = (A | ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(A);
    }
    private void ORA_EXT() {
        int addr = getExtendAdress();
        A = (A | ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(A);
    }
    private void ORA_IND() {
        int addr = getIndexedAddress();
        A = (A | ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(A);
    }

    // ORB
    private void ORB_IMM() {
        B = (B | fetchByte()) & 0xFF;
        updateZNFlags8(B);
    }
    private void ORB_DIR() {
        int addr = getDirectAdress();
        B = (B | ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(B);
    }
    private void ORB_EXT() {
        int addr = getExtendAdress();
        B = (B | ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(B);
    }
    private void ORB_IND() {
        int addr = getIndexedAddress();
        B = (B | ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(B);
    }

    // EORA
    private void EORA_IMM() {
        A = (A ^ fetchByte()) & 0xFF;
        updateZNFlags8(A);
    }
    private void EORA_DIR() {
        int addr = getDirectAdress();
        A = (A ^ ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(A);
    }
    private void EORA_EXT() {
        int addr = getExtendAdress();
        A = (A ^ ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(A);
    }
    private void EORA_IND() {
        int addr = getIndexedAddress();
        A = (A ^ ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(A);
    }

    // EORB
    private void EORB_IMM() {
        B = (B ^ fetchByte()) & 0xFF;
        updateZNFlags8(B);
    }
    private void EORB_DIR() {
        int addr = getDirectAdress();
        B = (B ^ ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(B);
    }
    private void EORB_EXT() {
        int addr = getExtendAdress();
        B = (B ^ ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(B);
    }
    private void EORB_IND() {
        int addr = getIndexedAddress();
        B = (B ^ ram.ReadFromMemory(addr)) & 0xFF;
        updateZNFlags8(B);
    }

    // BITA
    private void BITA_IMM() {
        int result = A & fetchByte();
        updateZNFlags8(result);
    }
    private void BITA_DIR() {
        int addr = getDirectAdress();
        int result = A & ram.ReadFromMemory(addr);
        updateZNFlags8(result);
    }
    private void BITA_EXT() {
        int addr = getExtendAdress();
        int result = A & ram.ReadFromMemory(addr);
        updateZNFlags8(result);
    }
    private void BITA_IND() {
        int addr = getIndexedAddress();
        int result = A & ram.ReadFromMemory(addr);
        updateZNFlags8(result);
    }

    // BITB
    private void BITB_IMM() {
        int result = B & fetchByte();
        updateZNFlags8(result);
    }
    private void BITB_DIR() {
        int addr = getDirectAdress();
        int result = B & ram.ReadFromMemory(addr);
        updateZNFlags8(result);
    }
    private void BITB_EXT() {
        int addr = getExtendAdress();
        int result = B & ram.ReadFromMemory(addr);
        updateZNFlags8(result);
    }
    private void BITB_IND() {
        int addr = getIndexedAddress();
        int result = B & ram.ReadFromMemory(addr);
        updateZNFlags8(result);
    }

    private void COMA() {
        A = (~A) & 0xFF;
        setFlag(C_FLAG);
        updateZNFlags8(A);
    }
    private void COMB() {
        B = (~B) & 0xFF;
        setFlag(C_FLAG);
        updateZNFlags8(B);
    }

    private void NEGA() {
        int orig = A & 0xFF;
        int result = (0 - orig) & 0xFF;
        A = result;
        if (result == 0) setFlag(Z_FLAG); else clearFlag(Z_FLAG);
        if (orig == 0x80) setFlag(V_FLAG); else clearFlag(V_FLAG);
        if ((result & 0x80) != 0) setFlag(N_FLAG); else clearFlag(N_FLAG);
        if (orig != 0) setFlag(C_FLAG); else clearFlag(C_FLAG);
    }
    private void NEGB() {
        int orig = B & 0xFF;
        int result = (0 - orig) & 0xFF;
        B = result;
        if (result == 0) setFlag(Z_FLAG); else clearFlag(Z_FLAG);
        if (orig == 0x80) setFlag(V_FLAG); else clearFlag(V_FLAG);
        if ((result & 0x80) != 0) setFlag(N_FLAG); else clearFlag(N_FLAG);
        if (orig != 0) setFlag(C_FLAG); else clearFlag(C_FLAG);
    }

    private void ASLA() {
        int old = A & 0xFF;
        A = (old << 1) & 0xFF;
        if ((old & 0x80) != 0) setFlag(C_FLAG); else clearFlag(C_FLAG);
        if (((A ^ old) & 0x80) != 0) setFlag(V_FLAG); else clearFlag(V_FLAG);
        updateZNFlags8(A);
    }
    private void ASLB() {
        int old = B & 0xFF;
        B = (old << 1) & 0xFF;
        if ((old & 0x80) != 0) setFlag(C_FLAG); else clearFlag(C_FLAG);
        if (((B ^ old) & 0x80) != 0) setFlag(V_FLAG); else clearFlag(V_FLAG);
        updateZNFlags8(B);
    }

    private void ASRA() {
        int old = A & 0xFF;
        int msb = old & 0x80;
        if ((old & 0x01) != 0) setFlag(C_FLAG); else clearFlag(C_FLAG);
        A = ((old >> 1) | msb) & 0xFF;
        clearFlag(V_FLAG);
        updateZNFlags8(A);
    }
    private void ASRB() {
        int old = B & 0xFF;
        int msb = old & 0x80;
        if ((old & 0x01) != 0) setFlag(C_FLAG); else clearFlag(C_FLAG);
        B = ((old >> 1) | msb) & 0xFF;
        clearFlag(V_FLAG);
        updateZNFlags8(B);
    }

    private void LSRA() {
        int old = A & 0xFF;
        if ((old & 0x01) != 0) setFlag(C_FLAG); else clearFlag(C_FLAG);
        A = (old >> 1) & 0x7F; // bit 7 becomes 0
        clearFlag(V_FLAG);
        updateZNFlags8(A);
    }
    private void LSRB() {
        int old = B & 0xFF;
        if ((old & 0x01) != 0) setFlag(C_FLAG); else clearFlag(C_FLAG);
        B = (old >> 1) & 0x7F;
        clearFlag(V_FLAG);
        updateZNFlags8(B);
    }

    private void ROLA() {
        int carryIn = isFlagSet(C_FLAG) ? 1 : 0;
        int newCarry = (A & 0x80) != 0 ? 1 : 0;
        A = ((A << 1) | carryIn) & 0xFF;
        if (newCarry == 1) setFlag(C_FLAG); else clearFlag(C_FLAG);
        clearFlag(V_FLAG);
        updateZNFlags8(A);
    }
    private void ROLB() {
        int carryIn = isFlagSet(C_FLAG) ? 1 : 0;
        int newCarry = (B & 0x80) != 0 ? 1 : 0;
        B = ((B << 1) | carryIn) & 0xFF;
        if (newCarry == 1) setFlag(C_FLAG); else clearFlag(C_FLAG);
        clearFlag(V_FLAG);
        updateZNFlags8(B);
    }

    private void RORA() {
        int carryIn = isFlagSet(C_FLAG) ? 0x80 : 0;
        int newCarry = A & 0x01;
        A = ((A >> 1) | carryIn) & 0xFF;
        if (newCarry != 0) setFlag(C_FLAG); else clearFlag(C_FLAG);
        clearFlag(V_FLAG);
        updateZNFlags8(A);
    }
    private void RORB() {
        int carryIn = isFlagSet(C_FLAG) ? 0x80 : 0;
        int newCarry = B & 0x01;
        B = ((B >> 1) | carryIn) & 0xFF;
        if (newCarry != 0) setFlag(C_FLAG); else clearFlag(C_FLAG);
        clearFlag(V_FLAG);
        updateZNFlags8(B);
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












    // ============================================================================
    // PART 7: STACK AND MISCELLANEOUS INSTRUCTIONS
    // ============================================================================

    // --- PSHS: Push Registers onto System Stack (S) ---
    // Uses your existing helpers pushWord/pushByte for cleaner code
    private void pshs() {
        int postbyte = fetchByte();

        // Order: PC, U, Y, X, DP, B, A, CC
        if ((postbyte & 0x80) != 0) pushWord(PC);
        if ((postbyte & 0x40) != 0) pushWord(U);
        if ((postbyte & 0x20) != 0) pushWord(Y);
        if ((postbyte & 0x10) != 0) pushWord(X);
        if ((postbyte & 0x08) != 0) pushByte(DP);
        if ((postbyte & 0x04) != 0) pushByte(B);
        if ((postbyte & 0x02) != 0) pushByte(A);
        if ((postbyte & 0x01) != 0) pushByte(CC);
    }

    // --- PSHU: Push Registers onto User Stack (U) ---
    // Uses her exact manual logic because you don't have 'pushUserWord' helpers
    private void pshu() {
        int postbyte = fetchByte();

        // Order: PC, S, Y, X, DP, B, A, CC
        if ((postbyte & 0x80) != 0) { // PC
            U = (U - 1) & 0xFFFF; ram.WriteInMemory(U, PC & 0xFF);       // Low
            U = (U - 1) & 0xFFFF; ram.WriteInMemory(U, (PC >> 8) & 0xFF); // High
        }
        if ((postbyte & 0x40) != 0) { // S
            U = (U - 1) & 0xFFFF; ram.WriteInMemory(U, S & 0xFF);
            U = (U - 1) & 0xFFFF; ram.WriteInMemory(U, (S >> 8) & 0xFF);
        }
        if ((postbyte & 0x20) != 0) { // Y
            U = (U - 1) & 0xFFFF; ram.WriteInMemory(U, Y & 0xFF);
            U = (U - 1) & 0xFFFF; ram.WriteInMemory(U, (Y >> 8) & 0xFF);
        }
        if ((postbyte & 0x10) != 0) { // X
            U = (U - 1) & 0xFFFF; ram.WriteInMemory(U, X & 0xFF);
            U = (U - 1) & 0xFFFF; ram.WriteInMemory(U, (X >> 8) & 0xFF);
        }
        if ((postbyte & 0x08) != 0) { // DP
            U = (U - 1) & 0xFFFF; ram.WriteInMemory(U, DP);
        }
        if ((postbyte & 0x04) != 0) { // B
            U = (U - 1) & 0xFFFF; ram.WriteInMemory(U, B);
        }
        if ((postbyte & 0x02) != 0) { // A
            U = (U - 1) & 0xFFFF; ram.WriteInMemory(U, A);
        }
        if ((postbyte & 0x01) != 0) { // CC
            U = (U - 1) & 0xFFFF; ram.WriteInMemory(U, CC);
        }
    }

    // --- PULS: Pull Registers from System Stack (S) ---
    // Uses your existing pullByte/pullWord helpers
    private void puls() {
        int postbyte = fetchByte();

        // Order: CC, A, B, DP, X, Y, U, PC
        if ((postbyte & 0x01) != 0) CC = pullByte();
        if ((postbyte & 0x02) != 0) A = pullByte();
        if ((postbyte & 0x04) != 0) B = pullByte();
        if ((postbyte & 0x08) != 0) DP = pullByte();
        if ((postbyte & 0x10) != 0) X = pullWord();
        if ((postbyte & 0x20) != 0) Y = pullWord();
        if ((postbyte & 0x40) != 0) U = pullWord();
        if ((postbyte & 0x80) != 0) PC = pullWord();
    }

    // --- PULU: Pull Registers from User Stack (U) ---
    // Uses her exact manual logic
    private void pulu() {
        int postbyte = fetchByte();

        // Order: CC, A, B, DP, X, Y, S, PC
        if ((postbyte & 0x01) != 0) { CC = ram.ReadFromMemory(U); U = (U + 1) & 0xFFFF; }
        if ((postbyte & 0x02) != 0) { A = ram.ReadFromMemory(U); U = (U + 1) & 0xFFFF; }
        if ((postbyte & 0x04) != 0) { B = ram.ReadFromMemory(U); U = (U + 1) & 0xFFFF; }
        if ((postbyte & 0x08) != 0) { DP = ram.ReadFromMemory(U); U = (U + 1) & 0xFFFF; }
        if ((postbyte & 0x10) != 0) { // X
            int high = ram.ReadFromMemory(U); U = (U + 1) & 0xFFFF;
            int low = ram.ReadFromMemory(U);  U = (U + 1) & 0xFFFF;
            X = (high << 8) | low;
        }
        if ((postbyte & 0x20) != 0) { // Y
            int high = ram.ReadFromMemory(U); U = (U + 1) & 0xFFFF;
            int low = ram.ReadFromMemory(U);  U = (U + 1) & 0xFFFF;
            Y = (high << 8) | low;
        }
        if ((postbyte & 0x40) != 0) { // S
            int high = ram.ReadFromMemory(U); U = (U + 1) & 0xFFFF;
            int low = ram.ReadFromMemory(U);  U = (U + 1) & 0xFFFF;
            S = (high << 8) | low;
        }
        if ((postbyte & 0x80) != 0) { // PC
            int high = ram.ReadFromMemory(U); U = (U + 1) & 0xFFFF;
            int low = ram.ReadFromMemory(U);  U = (U + 1) & 0xFFFF;
            PC = (high << 8) | low;
        }
    }

    // --- TFR: Transfer Register ---
    private void tfr() {
        int byteVal = fetchByte();
        int src = (byteVal >> 4) & 0x0F;
        int dest = byteVal & 0x0F;
        int val = getRegisterInternal(src);
        setRegisterInternal(dest, val);
    }

    // --- EXG: Exchange Register ---
    private void exg() {
        int byteVal = fetchByte();
        int r1 = (byteVal >> 4) & 0x0F;
        int r2 = byteVal & 0x0F;
        int val1 = getRegisterInternal(r1);
        int val2 = getRegisterInternal(r2);
        setRegisterInternal(r1, val2);
        setRegisterInternal(r2, val1);
    }

    // --- Helper for TFR/EXG (Replaces String decoding) ---
    private int getRegisterInternal(int code) {
        switch(code) {
            case 0: return getD();
            case 1: return X;
            case 2: return Y;
            case 3: return U;
            case 4: return S;
            case 5: return PC;
            case 8: return A;
            case 9: return B;
            case 10: return CC;
            case 11: return DP;
            default: return 0;
        }
    }

    // --- Helper for TFR/EXG ---
    private void setRegisterInternal(int code, int val) {
        switch(code) {
            case 0: setD(val); break;
            case 1: X = val & 0xFFFF; break;
            case 2: Y = val & 0xFFFF; break;
            case 3: U = val & 0xFFFF; break;
            case 4: S = val & 0xFFFF; break;
            case 5: PC = val & 0xFFFF; break;
            case 8: A = val & 0xFF; break;
            case 9: B = val & 0xFF; break;
            case 10: CC = val & 0xFF; break;
            case 11: DP = val & 0xFF; break;
        }
    }

    // --- Miscellaneous ---
    private void nop() { /* Do nothing */ }

    private void clrc() {
        clearFlag(C_FLAG);
    }

    private void sec() {
        setFlag(C_FLAG);
    }

    // --- Interrupts ---
    private void swi() {
        // PSHS all registers
        pushWord(PC); pushWord(U); pushWord(Y); pushWord(X);
        pushByte(DP); pushByte(B); pushByte(A); pushByte(CC);
        // Set Interrupt Mask flags
        setFlag(I_FLAG);
        setFlag(F_FLAG);
        // Jump to vector
        PC = ram.ReadWord(0xFFFA);
    }

    private void rti() {
        // PULS all registers (opposite of SWI)
        CC = pullByte(); A = pullByte(); B = pullByte(); DP = pullByte();
        X = pullWord(); Y = pullWord(); U = pullWord(); PC = pullWord();
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


// ================= 8-bit LOAD =================
//LDA
            case 0x86: LDA_IMM(); break;
            case 0x96: LDA_DIR(); break;
            case 0xB6: LDA_EXT(); break;
            case 0xA6: LDA_IND(); break; // FIXED: 0 instead of O

//LDB
            case 0xC6: LDB_IMM(); break;
            case 0xD6: LDB_DIR(); break;
            case 0xF6: LDB_EXT(); break;
            case 0xE6: LDB_IND(); break;

// STA
            case 0x97: STA_DIR(); break;
            case 0xB7: STA_EXT(); break;
            case 0xA7: STA_IND(); break; // FIXED: 0 instead of OX

//STB
            case 0xD7: STB_DIR(); break;
            case 0xF7: STB_EXT(); break;
            case 0xE7: STB_IND(); break;




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



            // ================= 8-bit Arithmetic =================
            case 0x8B: ADDA_IMM(); break;
            case 0x9B: ADDA_DIR(); break;
            case 0xBB: ADDA_EXT(); break;
            case 0xAB: ADDA_IND(); break;

            case 0xCB: ADDB_IMM(); break;
            case 0xDB: ADDB_DIR(); break;
            case 0xFB: ADDB_EXT(); break;
            case 0xEB: ADDB_IND(); break;

            case 0x89: ADCA_IMM(); break;
            case 0x99: ADCA_DIR(); break;
            case 0xB9: ADCA_EXT(); break;
            case 0xA9: ADCA_IND(); break;

            case 0xC9: ADCB_IMM(); break;
            case 0xD9: ADCB_DIR(); break;
            case 0xF9: ADCB_EXT(); break;
            case 0xE9: ADCB_IND(); break;

            case 0x80: SUBA_IMM(); break;
            case 0x90: SUBA_DIR(); break;
            case 0xB0: SUBA_EXT(); break;
            case 0xA0: SUBA_IND(); break;

            case 0xC0: SUBB_IMM(); break;
            case 0xD0: SUBB_DIR(); break;
            case 0xF0: SUBB_EXT(); break;
            case 0xE0: SUBB_IND(); break;

            case 0x82: SBCA_IMM(); break;
            case 0x92: SBCA_DIR(); break;
            case 0xB2: SBCA_EXT(); break;
            case 0xA2: SBCA_IND(); break;

            case 0xC2: SBCB_IMM(); break;
            case 0xD2: SBCB_DIR(); break;
            case 0xF2: SBCB_EXT(); break;
            case 0xE2: SBCB_IND(); break;

            case 0x81: CMPA_IMM(); break;
            case 0x91: CMPA_DIR(); break;
            case 0xB1: CMPA_EXT(); break;
            case 0xA1: CMPA_IND(); break;

            case 0xC1: CMPB_IMM(); break;
            case 0xD1: CMPB_DIR(); break;
            case 0xF1: CMPB_EXT(); break;
            case 0xE1: CMPB_IND(); break;

            // ================= INC / DEC =================
            case 0x4C: INCA(); break;
            case 0x5C: INCB(); break;
            case 0x4A: DECA(); break;
            case 0x5A: DECB(); break;

            // ================= CLR / TST =================
            case 0x4F: CLRA(); break;
            case 0x5F: CLRB(); break;
            case 0x4D: TSTA(); break;
            case 0x5D: TSTB(); break;





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




// ================= Logic =================
            case 0x84: ANDA_IMM(); break;
            case 0x94: ANDA_DIR(); break; // Fixed: Was 0x85 (Wrong)
            case 0xB4: ANDA_EXT(); break; // Fixed: Was 0x86 (Duplicate/Wrong)
            case 0xA4: ANDA_IND(); break; // Fixed: Was 0x87 (Wrong)

            case 0xC4: ANDB_IMM(); break;
            case 0xD4: ANDB_DIR(); break; // Fixed: Was 0xC5
            case 0xF4: ANDB_EXT(); break; // Fixed: Was 0xC6
            case 0xE4: ANDB_IND(); break; // Fixed: Was 0xC7

            case 0x8A: ORA_IMM(); break;
            case 0x9A: ORA_DIR(); break;
            case 0xBA: ORA_EXT(); break;
            case 0xAA: ORA_IND(); break;

// Missing ORB cases were likely here, adding them to be safe:
            case 0xCA: ORB_IMM(); break;
            case 0xDA: ORB_DIR(); break;
            case 0xFA: ORB_EXT(); break;
            case 0xEA: ORB_IND(); break;

            case 0x88: EORA_IMM(); break;
            case 0x98: EORA_DIR(); break;
            case 0xB8: EORA_EXT(); break;
            case 0xA8: EORA_IND(); break; // Fixed: Was 0x8F (Typo)

            case 0xC8: EORB_IMM(); break;
            case 0xD8: EORB_DIR(); break;
            case 0xF8: EORB_EXT(); break;
            case 0xE8: EORB_IND(); break;

            case 0x43: COMA(); break;
            case 0x53: COMB(); break;
            case 0x40: NEGA(); break;
            case 0x50: NEGB(); break;


            // ================= Shift =================
            case 0x48:
                ASLA();
                break;
            case 0x58:
                ASLB();
                break;
            case 0x47:
                ASRA();
                break;
            case 0x57:
                ASRB();
                break;
            case 0x44:
                LSRA();
                break;
            case 0x54:
                LSRB();
                break;
            case 0x49:
                ROLA();
                break;
            case 0x59:
                ROLB();
                break;
            case 0x46:
                RORA();
                break;
            case 0x56:
                RORB();
                break;





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



            // =================================================
            // PART 7: STACK AND MISCELLANEOUS
            // =================================================

            // --- Push / Pull Instructions ---
            case 0x34: pshs(); break; // Push System Stack
            case 0x36: pshu(); break; // Push User Stack
            case 0x35: puls(); break; // Pull System Stack
            case 0x37: pulu(); break; // Pull User Stack

            // --- Transfer / Exchange ---
            case 0x1F: tfr(); break;  // Transfer Register
            case 0x1E: exg(); break;  // Exchange Register

            // --- Flags & Misc ---
            case 0x12: nop(); break;  // No Operation
            case 0x0C: clrc(); break; // Clear Carry Flag (Project specific opcode)
            case 0x0D: sec(); break;  // Set Carry Flag (Project specific opcode)

            // --- Interrupts ---
            case 0x3F: swi(); break;  // Software Interrupt
            case 0x3B: rti(); break;  // Return from Interrupt




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
















// ==========================================
    //  SETTERS FOR GUI INTERACTION
    // ==========================================

    // 8-Bit Registers (Mask with 0xFF to ensure they stay 8-bit)
    public void setA(int val) { this.A = val & 0xFF; }
    public void setB(int val) { this.B = val & 0xFF; }
    public void setCC(int val) { this.CC = val & 0xFF; }
    public void setDP(int val) { this.DP = val & 0xFF; }

    // 16-Bit Registers (Mask with 0xFFFF to ensure they stay 16-bit)
    public void setX(int val) { this.X = val & 0xFFFF; }
    public void setY(int val) { this.Y = val & 0xFFFF; }
    public void setU(int val) { this.U = val & 0xFFFF; }
    public void setS(int val) { this.S = val & 0xFFFF; }

    // PC
    public void setPC(int address) {
        this.PC = address & 0xFFFF;
    }








}