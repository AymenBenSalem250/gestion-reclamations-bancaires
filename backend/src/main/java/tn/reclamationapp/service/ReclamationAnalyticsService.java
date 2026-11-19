package tn.reclamationapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.reclamationapp.dto.AnalyticsDTO;
import tn.reclamationapp.model.*;
import tn.reclamationapp.repository.ReclamationRepository;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ReclamationAnalyticsService {

    @Autowired
    private ReclamationRepository reclamationRepository;

    @Autowired
    private ReferenceDataService referenceDataService;

    private static final String[] MOIS_NOMS = {
        "Janvier", "Février", "Mars", "Avril", "Mai", "Juin", 
        "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
    };

    public AnalyticsDTO generateAnalytics(int year) {
        AnalyticsDTO dto = new AnalyticsDTO();
        
        dto.setTotal(reclamationRepository.countByYear(year));

        // 1. Répartition par Nature
        Map<String, Long> mapNature = new HashMap<>();
        for (NatureReclamant nat : referenceDataService.getAllNatureReclamants()) {
            mapNature.put(nat.getLibNat(), 0L);
        }
        fillMapWithQueryResults(mapNature, reclamationRepository.countByNatureReclamant(year));
        dto.setRepartitionNature(mapNature);

        // 2. Répartition par Canal
        Map<String, Long> mapCanal = new HashMap<>();
        for (Canal canal : referenceDataService.getAllCanals()) {
            mapCanal.put(canal.getLibCan(), 0L);
        }
        fillMapWithQueryResults(mapCanal, reclamationRepository.countByCanal(year));
        dto.setRepartitionCanal(mapCanal);

        // 3. Répartition par Objet
        Map<String, Long> mapObjet = new HashMap<>();
        for (Objet obj : referenceDataService.getAllObjets()) {
            mapObjet.put(obj.getLibObj(), 0L);
        }
        fillMapWithQueryResults(mapObjet, reclamationRepository.countByObjet(year));
        dto.setRepartitionObjet(mapObjet);

        // 4. Analyse dynamique des statuts
        processStatutsDynamically(year, dto);

        // 5. Evolution mensuelle dynamique (1 à 12)
        processEvolutionMensuelle(year, dto);

        // Traitement des délais
        processDelays(year, dto);

        // Traitement démographique (Genre et Age)
        processDemographics(year, dto);

        // Contrôles de cohérence (Ex: Total canaux = Total général)
        validateConsistency(dto);

        return dto;
    }

    private void fillMapWithQueryResults(Map<String, Long> map, List<Object[]> results) {
        for (Object[] row : results) {
            if (row[0] != null) {
                String key = row[0].toString();
                Long count = ((Number) row[1]).longValue();
                map.put(key, count);
            }
        }
    }

    private void processStatutsDynamically(int year, AnalyticsDTO dto) {
        Map<String, Long> rawStatutsCount = new HashMap<>();
        List<Object[]> queryResults = reclamationRepository.countByStatut(year);
        for (Object[] row : queryResults) {
            if (row[0] != null) {
                rawStatutsCount.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }

        long ouvertes = 0, cloturees = 0, favorables = 0, rejets = 0, enCours = 0, complements = 0;

        for (Statut s : referenceDataService.getAllStatuts()) {
            long count = rawStatutsCount.getOrDefault(s.getCodSta(), 0L);
            String lib = s.getLibSta() != null ? s.getLibSta().toLowerCase() : "";

            if (lib.contains("favorable") || lib.contains("accepté") || lib.contains("accepte")) {
                favorables += count;
            } else if (lib.contains("rejet") || lib.contains("refus")) {
                rejets += count;
            } else if (lib.contains("complément") || lib.contains("complement")) {
                complements += count;
            } else if (lib.contains("cours") || lib.contains("ouvert") || lib.contains("nouveau")) {
                enCours += count;
            }

            if (lib.contains("clôturé") || lib.contains("cloture") || lib.contains("fermé") || 
                lib.contains("favorable") || lib.contains("rejet") || lib.contains("refus") || lib.contains("accept")) {
                cloturees += count;
            } else {
                ouvertes += count;
            }
        }

        dto.setOuvertes(ouvertes);
        dto.setCloturees(cloturees);
        dto.setFavorables(favorables);
        dto.setRejets(rejets);
    }

    private void processEvolutionMensuelle(int year, AnalyticsDTO dto) {
        Map<Integer, Long> queryResults = new HashMap<>();
        for (Object[] row : reclamationRepository.countMonthlyEvolution(year)) {
            if (row[0] != null) {
                queryResults.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
            }
        }

        List<Map<String, Object>> evolutionList = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", MOIS_NOMS[month - 1]);
            monthData.put("total", queryResults.getOrDefault(month, 0L));
            evolutionList.add(monthData);
        }
        dto.setEvolutionMensuelle(evolutionList);
    }

    private void processDelays(int year, AnalyticsDTO dto) {
        List<Object[]> delays = reclamationRepository.findDelaysByYear(year);
        if (delays.isEmpty()) {
            dto.setDelaiMoyenJours(0);
            dto.setDelaiMinJours(0);
            dto.setDelaiMaxJours(0);
            return;
        }

        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        long sum = 0;

        for (Object[] row : delays) {
            Date datRec = (Date) row[0];
            Date datRep = (Date) row[1];
            
            long diffInMillies = Math.abs(datRep.getTime() - datRec.getTime());
            long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

            if (diffInDays < min) min = diffInDays;
            if (diffInDays > max) max = diffInDays;
            sum += diffInDays;
        }

        dto.setDelaiMinJours(min == Long.MAX_VALUE ? 0 : min);
        dto.setDelaiMaxJours(max == Long.MIN_VALUE ? 0 : max);
        dto.setDelaiMoyenJours((double) sum / delays.size());
    }

    private void processDemographics(int year, AnalyticsDTO dto) {
        List<Object[]> demographics = reclamationRepository.findDemographicsByYear(year);
        
        Map<String, Map<String, Long>> genreAgeMap = new HashMap<>();
        String[] genres = {"MASCULIN", "FEMININ", "INCONNU"};
        String[] ages = {"JEUNE (<30)", "INTERMEDIAIRE (30-50)", "SENIOR (>50)", "INCONNU"};
        
        for (String g : genres) {
            Map<String, Long> ageMap = new HashMap<>();
            for (String a : ages) {
                ageMap.put(a, 0L);
            }
            genreAgeMap.put(g, ageMap);
        }

        for (Object[] row : demographics) {
            Date datRec = (Date) row[0];
            Date datNaisRec = (Date) row[1];
            String genreRec = (String) row[2];
            Date clientDatNais = (Date) row[3];
            String sexClt = (String) row[4];

            Date finalDatNais = datNaisRec != null ? datNaisRec : clientDatNais;
            String finalGenre = genreRec != null && !genreRec.trim().isEmpty() ? genreRec : sexClt;

            String genreGroup = "INCONNU";
            if (finalGenre != null) {
                String g = finalGenre.toUpperCase();
                if (g.startsWith("M") || g.equals("HOMME")) genreGroup = "MASCULIN";
                else if (g.startsWith("F") || g.equals("FEMME")) genreGroup = "FEMININ";
            }

            String ageGroup = "INCONNU";
            if (finalDatNais != null && datRec != null) {
                Calendar calNais = Calendar.getInstance();
                calNais.setTime(finalDatNais);
                Calendar calRec = Calendar.getInstance();
                calRec.setTime(datRec);

                int age = calRec.get(Calendar.YEAR) - calNais.get(Calendar.YEAR);
                if (calNais.get(Calendar.MONTH) > calRec.get(Calendar.MONTH) ||
                    (calNais.get(Calendar.MONTH) == calRec.get(Calendar.MONTH) && calNais.get(Calendar.DAY_OF_MONTH) > calRec.get(Calendar.DAY_OF_MONTH))) {
                    age--;
                }

                if (age < 30) ageGroup = "JEUNE (<30)";
                else if (age <= 50) ageGroup = "INTERMEDIAIRE (30-50)";
                else ageGroup = "SENIOR (>50)";
            }

            long currentCount = genreAgeMap.get(genreGroup).get(ageGroup);
            genreAgeMap.get(genreGroup).put(ageGroup, currentCount + 1);
        }

        dto.setRepartitionGenreAge(genreAgeMap);
    }

    private void validateConsistency(AnalyticsDTO dto) {
        long totalCanaux = dto.getRepartitionCanal().values().stream().mapToLong(Long::longValue).sum();
        long totalNature = dto.getRepartitionNature().values().stream().mapToLong(Long::longValue).sum();
        long totalObjet = dto.getRepartitionObjet().values().stream().mapToLong(Long::longValue).sum();

        long maxTotal = Math.max(totalCanaux, Math.max(totalNature, totalObjet));
        
        if (dto.getTotal() != maxTotal) {
            System.err.println("Incohérence détectée: Total calculé=" + dto.getTotal() + " | Max des répartitions=" + maxTotal);
        }
    }
}
