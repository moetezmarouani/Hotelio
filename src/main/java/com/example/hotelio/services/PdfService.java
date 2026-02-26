package com.example.hotelio.services;

import com.example.hotelio.entities.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PdfService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.BLACK);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
    private static final Font BOLD_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLACK);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.GRAY);

    /**
     * Génère une facture PDF pour une réservation
     */
    public boolean genererFacture(Reservation reservation, User client, Chambre chambre, String cheminFichier) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
            document.open();

            // En-tête avec logo et infos hôtel
            ajouterEntete(document);

            // Séparateur
            ajouterLigne(document);

            // Titre
            Paragraph titre = new Paragraph("FACTURE", TITLE_FONT);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(10);
            document.add(titre);

            // Numéro de facture et date
            Paragraph refDate = new Paragraph();
            refDate.add(new Chunk("Facture N° : ", BOLD_FONT));
            refDate.add(new Chunk("FAC-" + reservation.getId() + "-" + LocalDate.now().getYear(), NORMAL_FONT));
            refDate.add(Chunk.NEWLINE);
            refDate.add(new Chunk("Date : ", BOLD_FONT));
            refDate.add(new Chunk(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), NORMAL_FONT));
            refDate.setSpacingAfter(20);
            document.add(refDate);

            // Informations client et hôtel en deux colonnes
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20);

            // Colonne Client
            PdfPCell clientCell = new PdfPCell();
            clientCell.setBorder(Rectangle.NO_BORDER);
            Paragraph clientInfo = new Paragraph();
            clientInfo.add(new Chunk("CLIENT\n", HEADER_FONT));
            clientInfo.add(new Chunk(client.getNomComplet() + "\n", NORMAL_FONT));
            clientInfo.add(new Chunk(client.getEmail() + "\n", NORMAL_FONT));
            clientInfo.add(new Chunk(client.getTelephone() != null ? client.getTelephone() : "", NORMAL_FONT));
            clientCell.addElement(clientInfo);
            infoTable.addCell(clientCell);

            // Colonne Hôtel
            PdfPCell hotelCell = new PdfPCell();
            hotelCell.setBorder(Rectangle.NO_BORDER);
            hotelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph hotelInfo = new Paragraph();
            hotelInfo.add(new Chunk("HOTELIO\n", HEADER_FONT));
            hotelInfo.add(new Chunk("123 Avenue Habib Bourguiba\n", NORMAL_FONT));
            hotelInfo.add(new Chunk("Tunis, Tunisie\n", NORMAL_FONT));
            hotelInfo.add(new Chunk("Tél: +216 71 123 456\n", NORMAL_FONT));
            hotelInfo.add(new Chunk("contact@hotelio.com", NORMAL_FONT));
            hotelCell.addElement(hotelInfo);
            infoTable.addCell(hotelCell);

            document.add(infoTable);

            // Détails de la réservation
            Paragraph detailsTitre = new Paragraph("Détails de la réservation", HEADER_FONT);
            detailsTitre.setSpacingBefore(10);
            detailsTitre.setSpacingAfter(10);
            document.add(detailsTitre);

            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingAfter(20);

            ajouterLigneDetail(detailsTable, "Réservation N°", String.valueOf(reservation.getId()));
            ajouterLigneDetail(detailsTable, "Chambre", chambre.getNumero() + " - " + chambre.getType());
            ajouterLigneDetail(detailsTable, "Date d'arrivée", reservation.getDateCheckin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            ajouterLigneDetail(detailsTable, "Date de départ", reservation.getDateCheckout().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            ajouterLigneDetail(detailsTable, "Nombre de nuits", String.valueOf(reservation.getNombreNuits()));
            ajouterLigneDetail(detailsTable, "Nombre de personnes", String.valueOf(reservation.getNombrePersonnes()));

            document.add(detailsTable);

            // Tableau des prix
            Paragraph prixTitre = new Paragraph("Détail des prix", HEADER_FONT);
            prixTitre.setSpacingBefore(10);
            prixTitre.setSpacingAfter(10);
            document.add(prixTitre);

            PdfPTable prixTable = new PdfPTable(4);
            prixTable.setWidthPercentage(100);
            prixTable.setWidths(new float[]{3, 1, 2, 2});

            // En-tête du tableau
            ajouterCelluleEntete(prixTable, "Description");
            ajouterCelluleEntete(prixTable, "Qté");
            ajouterCelluleEntete(prixTable, "Prix unitaire");
            ajouterCelluleEntete(prixTable, "Total");

            // Ligne de détail
            ajouterCellule(prixTable, "Chambre " + chambre.getNumero() + " - " + chambre.getType());
            ajouterCellule(prixTable, String.valueOf(reservation.getNombreNuits()));
            ajouterCellule(prixTable, String.format("%.2f DT", chambre.getPrixParNuit()));
            ajouterCellule(prixTable, String.format("%.2f DT", reservation.getPrixTotal()));

            document.add(prixTable);

            // Total
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setWidths(new float[]{3, 1});
            totalTable.setSpacingBefore(10);

            PdfPCell labelCell = new PdfPCell(new Phrase("TOTAL À PAYER", BOLD_FONT));
            labelCell.setBorder(Rectangle.NO_BORDER);
            labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            labelCell.setPaddingRight(10);
            totalTable.addCell(labelCell);

            PdfPCell montantCell = new PdfPCell(new Phrase(String.format("%.2f DT", reservation.getPrixTotal()), HEADER_FONT));
            montantCell.setBorder(Rectangle.TOP);
            montantCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            montantCell.setBackgroundColor(new BaseColor(240, 240, 240));
            montantCell.setPadding(10);
            totalTable.addCell(montantCell);

            document.add(totalTable);

            // Conditions de paiement
            Paragraph conditions = new Paragraph();
            conditions.setSpacingBefore(30);
            conditions.add(new Chunk("Conditions de paiement\n", BOLD_FONT));
            conditions.add(new Chunk("• Paiement à l'arrivée\n", SMALL_FONT));
            conditions.add(new Chunk("• Annulation gratuite jusqu'à 48h avant l'arrivée\n", SMALL_FONT));
            conditions.add(new Chunk("• Carte d'identité requise lors du check-in\n", SMALL_FONT));
            document.add(conditions);

            // Pied de page
            Paragraph footer = new Paragraph();
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            footer.add(new Chunk("Merci de votre confiance !\n", BOLD_FONT));
            footer.add(new Chunk("HotelIO - Votre confort est notre priorité", SMALL_FONT));
            document.add(footer);

            document.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Génère une confirmation de réservation PDF
     */
    public boolean genererConfirmation(Reservation reservation, User client, Chambre chambre, String cheminFichier) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(cheminFichier));
            document.open();

            // En-tête
            ajouterEntete(document);
            ajouterLigne(document);

            // Titre
            Paragraph titre = new Paragraph("CONFIRMATION DE RÉSERVATION", TITLE_FONT);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(20);
            document.add(titre);

            // Message de confirmation
            Paragraph message = new Paragraph();
            message.add(new Chunk("Cher(e) " + client.getNomComplet() + ",\n\n", NORMAL_FONT));
            message.add(new Chunk("Nous avons le plaisir de confirmer votre réservation chez HotelIO.\n\n", NORMAL_FONT));
            message.setSpacingAfter(20);
            document.add(message);

            // Détails de la réservation
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(80);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);

            ajouterLigneDetail(table, "Numéro de réservation", "RES-" + reservation.getId());
            ajouterLigneDetail(table, "Chambre", chambre.getNumero() + " - " + chambre.getType());
            ajouterLigneDetail(table, "Arrivée", reservation.getDateCheckin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " (à partir de 14h00)");
            ajouterLigneDetail(table, "Départ", reservation.getDateCheckout().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " (avant 11h00)");
            ajouterLigneDetail(table, "Nombre de nuits", String.valueOf(reservation.getNombreNuits()));
            ajouterLigneDetail(table, "Nombre de personnes", String.valueOf(reservation.getNombrePersonnes()));
            ajouterLigneDetail(table, "Prix total", String.format("%.2f DT", reservation.getPrixTotal()));

            document.add(table);

            // Informations pratiques
            Paragraph infos = new Paragraph();
            infos.setSpacingBefore(30);
            infos.add(new Chunk("Informations pratiques\n\n", HEADER_FONT));
            infos.add(new Chunk("Adresse : 123 Avenue Habib Bourguiba, Tunis\n", NORMAL_FONT));
            infos.add(new Chunk("Téléphone : +216 71 123 456\n", NORMAL_FONT));
            infos.add(new Chunk("Email : contact@hotelio.com\n\n", NORMAL_FONT));
            infos.add(new Chunk("Services inclus : WiFi gratuit, Petit-déjeuner, Parking\n", SMALL_FONT));
            document.add(infos);

            // Footer
            Paragraph footer = new Paragraph();
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(40);
            footer.add(new Chunk("Nous vous souhaitons un excellent séjour !\n", BOLD_FONT));
            footer.add(new Chunk("L'équipe HotelIO", SMALL_FONT));
            document.add(footer);

            document.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Méthodes utilitaires
    private void ajouterEntete(Document document) throws DocumentException {
        Paragraph entete = new Paragraph();
        entete.add(new Chunk("🏨 HOTELIO\n", new Font(Font.FontFamily.HELVETICA, 28, Font.BOLD, new BaseColor(52, 152, 219))));
        entete.add(new Chunk("Votre confort, notre passion", SMALL_FONT));
        entete.setAlignment(Element.ALIGN_CENTER);
        entete.setSpacingAfter(10);
        document.add(entete);
    }

    private void ajouterLigne(Document document) throws DocumentException {
        LineSeparator line = new LineSeparator();
        line.setLineColor(new BaseColor(200, 200, 200));
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);
    }

    private void ajouterLigneDetail(PdfPTable table, String label, String valeur) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(8);
        table.addCell(labelCell);

        PdfPCell valeurCell = new PdfPCell(new Phrase(valeur, NORMAL_FONT));
        valeurCell.setBorder(Rectangle.NO_BORDER);
        valeurCell.setPadding(8);
        table.addCell(valeurCell);
    }

    private void ajouterCelluleEntete(PdfPTable table, String texte) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, BOLD_FONT));
        cell.setBackgroundColor(new BaseColor(52, 152, 219));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(10);
        Font whiteFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        cell.setPhrase(new Phrase(texte, whiteFont));
        table.addCell(cell);
    }

    private void ajouterCellule(PdfPTable table, String texte) {
        PdfPCell cell = new PdfPCell(new Phrase(texte, NORMAL_FONT));
        cell.setPadding(8);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }
}