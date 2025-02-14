class DeadLoop {

    void deadLoop() {
        int x = 1;
        int y = 0;
        int z = 100;
        while (x > y) {
            use(z);
        }
        dead();
    }

    void dead() {

    }

    void use(int n) {

    }

    public static void main(String args[]) {

    }

}