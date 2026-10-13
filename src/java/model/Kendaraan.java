
package model;

public class Kendaraan {

    private int id;
    private String nama;
    private String deskripsi;
    private String kategori;
    private double hargaPerHari;
    private double deposit;
    private int stok;
    private String fotoPath;

    public Kendaraan() {
    }

    public Kendaraan(int id, String nama, String deskripsi,
            String kategori, double hargaPerHari,
            double deposit, int stok, String fotoPath) {
        this.id = id;
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.kategori = kategori;
        this.hargaPerHari = hargaPerHari;
        this.deposit = deposit;
        this.stok = stok;
        this.fotoPath = fotoPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public double getHargaPerHari() {
        return hargaPerHari;
    }

    public void setHargaPerHari(double hargaPerHari) {
        this.hargaPerHari = hargaPerHari;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }
}
