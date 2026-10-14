package br.com.phoebus.payments.demo.domain;

public enum AquirerEnum {
    ADIQ(1),
    CIELO(2),
    STONE(3),
    PRISMA(4),
    AMEX(5),
    OTHER(6);


    private int id;

    AquirerEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name();
    }

    public static AquirerEnum getById(int id) {
        for(AquirerEnum e : values()) {
            if(e.id == id)
                return e;
        }
        return null;
    }

}
