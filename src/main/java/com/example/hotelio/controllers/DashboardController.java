package com.example.hotelio.controllers;

import com.example.hotelio.entities.*;
import com.example.hotelio.enums.*;
import com.example.hotelio.services.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private Button refreshButton;

    // Labels pour les cartes statistiques
    @FXML private Label totalReservationsLabel;
    @FXML private Label revenusLabel;
    @FXML private Label tauxOccupationLabel;
    @FXML private Label clientsActifsLabel;

    // Graphiques
    @FXML private PieChart statutsPieChart;
    @FXML private PieChart chambresTypesPieChart;
    @FXML private BarChart<String, Number> revenusBarChart;
    @FXML private TableView<ClientStats> topClientsTable;

    private ReservationService reservationService = new ReservationService();
    private ChambreService chambreService = new ChambreService();
    private UserService userService = new UserService();
    private FideliteService fideliteService = new FideliteService();

    @FXML
    public void initialize() {
        setupTopClientsTable();
        loadDashboardData();
    }

    @FXML
    private void handleRefresh() {
        loadDashboardData();
    }

    private void loadDashboardData() {
        loadStatistiquesCartes();
        loadStatutsChart();
        loadChambresTypesChart();
        loadRevenusChart();
        loadTopClients();
    }

    /**
     * Charge les statistiques des cartes en haut
     */
    private void loadStatistiquesCartes() {
        List<Reservation> reservations = reservationService.obtenirToutesLesReservations();
        List<Chambre> chambres = chambreService.obtenirToutesLesChambres();

        // Total réservations
        totalReservationsLabel.setText(String.valueOf(reservations.size()));

        // Revenus totaux
        double revenus = reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE ||
                        r.getStatut() == StatutReservation.TERMINEE)
                .mapToDouble(Reservation::getPrixTotal)
                .sum();
        revenusLabel.setText(String.format("%.2f DT", revenus));

        // Taux d'occupation
        long chambresOccupees = chambres.stream()
                .filter(c -> c.getStatut() == StatutChambre.OCCUPEE)
                .count();
        double tauxOccupation = (chambres.size() > 0)
                ? (chambresOccupees * 100.0 / chambres.size())
                : 0;
        tauxOccupationLabel.setText(String.format("%.1f%%", tauxOccupation));

        // Clients actifs (clients avec au moins une réservation)
        Set<Integer> clientsActifs = reservations.stream()
                .map(Reservation::getUserId)
                .collect(Collectors.toSet());
        clientsActifsLabel.setText(String.valueOf(clientsActifs.size()));
    }

    /**
     * Graphique camembert des statuts de réservations
     */
    private void loadStatutsChart() {
        List<Reservation> reservations = reservationService.obtenirToutesLesReservations();

        Map<StatutReservation, Long> statutsCount = reservations.stream()
                .collect(Collectors.groupingBy(
                        Reservation::getStatut,
                        Collectors.counting()
                ));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<StatutReservation, Long> entry : statutsCount.entrySet()) {
            String label = entry.getKey().name() + " (" + entry.getValue() + ")";
            pieChartData.add(new PieChart.Data(label, entry.getValue()));
        }

        statutsPieChart.setData(pieChartData);
        statutsPieChart.setTitle("");

        // Couleurs personnalisées
        applyCSSColors(statutsPieChart, statutsCount.keySet());
    }

    /**
     * Graphique camembert des types de chambres les plus réservées
     */
    private void loadChambresTypesChart() {
        List<Reservation> reservations = reservationService.obtenirToutesLesReservations();

        Map<TypeChambre, Long> typesCount = new HashMap<>();

        for (Reservation reservation : reservations) {
            Chambre chambre = chambreService.obtenirChambreParId(reservation.getChambreId());
            if (chambre != null) {
                typesCount.merge(chambre.getType(), 1L, Long::sum);
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<TypeChambre, Long> entry : typesCount.entrySet()) {
            String label = entry.getKey().name() + " (" + entry.getValue() + ")";
            pieChartData.add(new PieChart.Data(label, entry.getValue()));
        }

        chambresTypesPieChart.setData(pieChartData);
        chambresTypesPieChart.setTitle("");
    }

    /**
     * Graphique en barres des revenus par mois
     */
    private void loadRevenusChart() {
        List<Reservation> reservations = reservationService.obtenirToutesLesReservations();

        // Grouper par mois
        Map<String, Double> revenuParMois = new TreeMap<>();

        for (Reservation reservation : reservations) {
            if (reservation.getStatut() == StatutReservation.CONFIRMEE ||
                    reservation.getStatut() == StatutReservation.TERMINEE) {

                String mois = reservation.getDateCheckin().getMonth()
                        .getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " +
                        reservation.getDateCheckin().getYear();

                revenuParMois.merge(mois, reservation.getPrixTotal(), Double::sum);
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenus");

        // Prendre les 6 derniers mois
        List<Map.Entry<String, Double>> derniersMois = new ArrayList<>(revenuParMois.entrySet());
        int start = Math.max(0, derniersMois.size() - 6);

        for (int i = start; i < derniersMois.size(); i++) {
            Map.Entry<String, Double> entry = derniersMois.get(i);
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        revenusBarChart.getData().clear();
        revenusBarChart.getData().add(series);
        revenusBarChart.setLegendVisible(false);
    }

    /**
     * Charge le top 5 des clients
     */
    private void loadTopClients() {
        List<User> clients = userService.obtenirTousLesUtilisateurs().stream()
                .filter(u -> u.getTypeUtilisateur().equals("CLIENT"))
                .collect(Collectors.toList());

        List<ClientStats> clientStatsList = new ArrayList<>();

        for (User client : clients) {
            List<Reservation> reservationsClient = reservationService
                    .obtenirReservationsParUtilisateur(client.getId());

            if (!reservationsClient.isEmpty()) {
                long nbReservations = reservationsClient.size();

                double depensesTotales = reservationsClient.stream()
                        .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE ||
                                r.getStatut() == StatutReservation.TERMINEE)
                        .mapToDouble(Reservation::getPrixTotal)
                        .sum();

                String niveau = fideliteService.obtenirNiveau(client.getId());
                String niveauAvecEmoji = fideliteService.getEmojiNiveau(niveau) + " " + niveau;

                clientStatsList.add(new ClientStats(
                        client.getNomComplet(),
                        nbReservations,
                        depensesTotales,
                        niveauAvecEmoji
                ));
            }
        }

        // Trier par dépenses totales (décroissant)
        clientStatsList.sort((c1, c2) -> Double.compare(c2.getDepensesTotales(), c1.getDepensesTotales()));

        // Prendre le top 5
        List<ClientStats> top5 = clientStatsList.stream()
                .limit(5)
                .collect(Collectors.toList());

        // Ajouter le rang
        for (int i = 0; i < top5.size(); i++) {
            top5.get(i).setRang(i + 1);
        }

        topClientsTable.getItems().setAll(top5);
    }

    /**
     * Configure la table des top clients
     */
    private void setupTopClientsTable() {
        TableColumn<ClientStats, Integer> rangCol = (TableColumn<ClientStats, Integer>) topClientsTable.getColumns().get(0);
        rangCol.setCellValueFactory(new PropertyValueFactory<>("rang"));
        rangCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ClientStats, String> nomCol = (TableColumn<ClientStats, String>) topClientsTable.getColumns().get(1);
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<ClientStats, Long> resaCol = (TableColumn<ClientStats, Long>) topClientsTable.getColumns().get(2);
        resaCol.setCellValueFactory(new PropertyValueFactory<>("nombreReservations"));
        resaCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<ClientStats, Double> depensesCol = (TableColumn<ClientStats, Double>) topClientsTable.getColumns().get(3);
        depensesCol.setCellValueFactory(new PropertyValueFactory<>("depensesTotales"));
        depensesCol.setStyle("-fx-alignment: CENTER_RIGHT;");
        depensesCol.setCellFactory(col -> new TableCell<ClientStats, Double>() {
            @Override
            protected void updateItem(Double montant, boolean empty) {
                super.updateItem(montant, empty);
                if (empty || montant == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", montant));
                }
            }
        });

        TableColumn<ClientStats, String> niveauCol = (TableColumn<ClientStats, String>) topClientsTable.getColumns().get(4);
        niveauCol.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        niveauCol.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Applique des couleurs CSS personnalisées au PieChart
     */
    private void applyCSSColors(PieChart chart, Set<StatutReservation> statuts) {
        // Les couleurs seront appliquées automatiquement par JavaFX
        // Optionnel: personnaliser via CSS externe
    }

    /**
     * Classe interne pour les statistiques clients
     */
    public static class ClientStats {
        private int rang;
        private String nom;
        private long nombreReservations;
        private double depensesTotales;
        private String niveau;

        public ClientStats(String nom, long nombreReservations, double depensesTotales, String niveau) {
            this.nom = nom;
            this.nombreReservations = nombreReservations;
            this.depensesTotales = depensesTotales;
            this.niveau = niveau;
        }

        // Getters et Setters
        public int getRang() { return rang; }
        public void setRang(int rang) { this.rang = rang; }

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }

        public long getNombreReservations() { return nombreReservations; }
        public void setNombreReservations(long nombreReservations) {
            this.nombreReservations = nombreReservations;
        }

        public double getDepensesTotales() { return depensesTotales; }
        public void setDepensesTotales(double depensesTotales) {
            this.depensesTotales = depensesTotales;
        }

        public String getNiveau() { return niveau; }
        public void setNiveau(String niveau) { this.niveau = niveau; }
    }
}