// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.

    @8192
    D=A
    @num_of_pixel
    M=D // num_of_pixel=8192

    @crrnt
    M=0

(POLLING)
    @KBD
    D=M // D=M[24576]
    @PUSHED
    D;JNE // if D != 0 then -> PUSHED
(NOT_PUSHED)
    @R0 // value to set
    M=0 // R0=0
    @JUDGE
    0;JMP
(PUSHED)
    @R0
    M=1 // R0=1
(JUDGE)
    @crrnt
    D=M // D=crrnt
    @R0
    D=D-M // D=crrnt-R0
    @POLLING
    D;JEQ // if crrnt==R0 then -> POLLING
    @i // 18
    M=0 // i=0
    @R0
    D=M // D=R0
    @crrnt
    M=D // crrnt=R0
    @FILL
    D;JNE // if R0 != 0 then -> FILL
(EMPTY)
    @i
    D=M // D=i
    @num_of_pixel
    D=M-D // D=8192-i
    @POLLING
    D;JLT // if 8192-i < 0 then -> POLLING
    @SCREEN
    A=A+D // A=16834+D
    M=0
    @i
    M=M+1 // i++
    @EMPTY
    0;JMP
(FILL)
    @i
    D=M // D=i
    @num_of_pixel
    D=M-D // D=8192-i
    @POLLING
    D;JLT // if 8192-i < 0 then -> POLLING
    @SCREEN
    A=A+D // A=16834+D
    M=-1
    @i
    M=M+1 // i++
    @FILL
    0;JMP