package tn.reclamationapp.service;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.reclamationapp.dto.AnalyticsDTO;
import tn.reclamationapp.model.Reclamation;
import tn.reclamationapp.model.ReclamationDt;
import tn.reclamationapp.repository.ReclamationRepository;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class XmlROG760Service {

    @Autowired
    private ReclamationAnalyticsService analyticsService;

    @Autowired
    private ReclamationService reclamationService;

    @Autowired
    private ReclamationRepository reclamationRepository;

    public String generateBCT760Xml(int year) throws Exception {
        
        // 1. Générer les statistiques
        AnalyticsDTO analytics = analyticsService.generateAnalytics(year);

        // 2. Mapper les statistiques vers ReclamationDt
        ReclamationDt reclamationDt = new ReclamationDt();
        reclamationDt.setDateAnnexe(new Date());

        reclamationDt.setReclamationsByNat(analytics.getRepartitionNature());
        reclamationDt.setReclamationsByCanal(analytics.getRepartitionCanal());
        reclamationDt.setReclamationsByGenreAndAge(analytics.getRepartitionGenreAge());

        // Extraction des statistiques spécifiques d'âge et de statut si nécessaire
        int ageJeune = 0, ageIntermediate = 0, ageMiddle = 0;
        if (analytics.getRepartitionGenreAge() != null) {
            for (Map<String, Long> mapAge : analytics.getRepartitionGenreAge().values()) {
                ageJeune += mapAge.getOrDefault("JEUNE (<30)", 0L);
                ageIntermediate += mapAge.getOrDefault("INTERMEDIAIRE (30-50)", 0L);
                ageMiddle += mapAge.getOrDefault("SENIOR (>50)", 0L);
            }
        }
        reclamationDt.setAgeJeune(ageJeune);
        reclamationDt.setAgeIntermediate(ageIntermediate);
        reclamationDt.setAgeMiddle(ageMiddle);

        Map<String, Long> stats = new HashMap<>();
        stats.put("Total", analytics.getTotal());
        stats.put("DelaiMoyen", (long) analytics.getDelaiMoyenJours());
        stats.put("DelaiMin", analytics.getDelaiMinJours());
        stats.put("DelaiMax", analytics.getDelaiMaxJours());
        reclamationDt.setStatistiques(stats);

        // 3. Générer le XML avec JAXB
        JAXBContext context = JAXBContext.newInstance(ReclamationDt.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter sw = new StringWriter();
        marshaller.marshal(reclamationDt, sw);
        String xmlContent = sw.toString();

        // 4. Marquer les réclamations comme générées pour éviter les doublons
        Date start = java.sql.Date.valueOf(year + "-01-01");
        Date end = java.sql.Date.valueOf(year + "-12-31");
        
        List<Reclamation> reclamations = reclamationRepository.findByDatRecBetween(start, end);
        for (Reclamation r : reclamations) {
            if (!r.isXmlGenerated()) {
                reclamationService.markAsXmlGenerated(r.getRefRec(), "SYSTEM_BCT");
            }
        }

        return xmlContent;
    }
}
