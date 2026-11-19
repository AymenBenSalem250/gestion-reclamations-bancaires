package tn.reclamationapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.reclamationapp.model.*;
import tn.reclamationapp.repository.*;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReferenceDataService {

    @Autowired
    private StatutRepository statutRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private ObjetRepository objetRepository;
    
    @Autowired
    private CanalRepository canalRepository;
    
    @Autowired
    private NatureReclamantRepository natureReclamantRepository;
    
    @Autowired
    private ParamGenRepository paramGenRepository;
    
    @Autowired
    private NotificationTypeRepository notificationTypeRepository;
    
    // Statut operations
    public List<Statut> getAllStatuts() {
        return statutRepository.findAll();
    }

    public Page<Statut> getStatutsPage(int page, int size) {
        return statutRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "codSta")));
    }
    
    public Optional<Statut> getStatutByCode(String codSta) {
        return statutRepository.findByCodSta(codSta);
    }
    
    public Statut createStatut(Statut statut) {
        return statutRepository.save(statut);
    }
    
    public Statut updateStatut(String codSta, Statut statutDetails) {
        Statut statut = statutRepository.findByCodSta(codSta)
                .orElseThrow(() -> new RuntimeException("Statut not found"));
        statut.setLibSta(statutDetails.getLibSta());
        statut.setMsgSta(statutDetails.getMsgSta());
        statut.setMsg2Sta(statutDetails.getMsg2Sta());
        return statutRepository.save(statut);
    }
    
    public void deleteStatut(String codSta) {
        Statut statut = statutRepository.findByCodSta(codSta)
                .orElseThrow(() -> new RuntimeException("Statut not found"));
        statutRepository.delete(statut);
    }
    
    // Service operations
    public List<tn.reclamationapp.model.Service> getAllServices() {
        return serviceRepository.findAll();
    }

    public Page<tn.reclamationapp.model.Service> getServicesPage(int page, int size) {
        return serviceRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "codSer")));
    }
    
    public Optional<tn.reclamationapp.model.Service> getServiceByCode(String codSer) {
        return serviceRepository.findByCodSer(codSer);
    }
    
    public tn.reclamationapp.model.Service createService(tn.reclamationapp.model.Service service) {
        return serviceRepository.save(service);
    }
    
    public tn.reclamationapp.model.Service updateService(String codSer, tn.reclamationapp.model.Service serviceDetails) {
        tn.reclamationapp.model.Service service = serviceRepository.findByCodSer(codSer)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        service.setLibSer(serviceDetails.getLibSer());
        service.setEmail1Ser(serviceDetails.getEmail1Ser());
        service.setEmail2Ser(serviceDetails.getEmail2Ser());
        return serviceRepository.save(service);
    }
    
    public void deleteService(String codSer) {
        tn.reclamationapp.model.Service service = serviceRepository.findByCodSer(codSer)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        serviceRepository.delete(service);
    }
    
    // Objet operations
    public List<Objet> getAllObjets() {
        return objetRepository.findAll();
    }

    public Page<Objet> getObjetsPage(int page, int size) {
        return objetRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }
    
    public Optional<Objet> getObjetByCode(String codObj) {
        return objetRepository.findByCodObj(codObj);
    }
    
    public List<Objet> getObjetsByService(String codSer) {
        return objetRepository.findByService_CodSer(codSer);
    }
    
    public Objet createObjet(Objet objet) {
        return objetRepository.save(objet);
    }
    
    public Objet updateObjet(Long id, Objet objetDetails) {
        Objet objet = objetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Objet not found"));
        objet.setLibObj(objetDetails.getLibObj());
        objet.setCodObj(objetDetails.getCodObj());
        objet.setService(objetDetails.getService());
        return objetRepository.save(objet);
    }
    
    public void deleteObjet(Long id) {
        Objet objet = objetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Objet not found"));
        objetRepository.delete(objet);
    }
    
    // Canal operations
    public List<Canal> getAllCanals() {
        return canalRepository.findAll();
    }

    public Page<Canal> getCanalsPage(int page, int size) {
        return canalRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }
    
    public Optional<Canal> getCanalById(Long id) {
        return canalRepository.findById(id);
    }
    
    public Canal createCanal(Canal canal) {
        return canalRepository.save(canal);
    }
    
    public Canal updateCanal(Long id, Canal canalDetails) {
        Canal canal = canalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canal not found"));
        canal.setLibCan(canalDetails.getLibCan());
        return canalRepository.save(canal);
    }
    
    public void deleteCanal(Long id) {
        Canal canal = canalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canal not found"));
        canalRepository.delete(canal);
    }
    
    // NatureReclamant operations
    public List<NatureReclamant> getAllNatureReclamants() {
        return natureReclamantRepository.findAll();
    }

    public Page<NatureReclamant> getNatureRelamantsPage(int page, int size) {
        return natureReclamantRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }
    
    public Optional<NatureReclamant> getNatureReclamantById(Long id) {
        return natureReclamantRepository.findById(id);
    }
    
    public NatureReclamant createNatureReclamant(NatureReclamant nature) {
        return natureReclamantRepository.save(nature);
    }
    
    public NatureReclamant updateNatureReclamant(Long id, NatureReclamant natureDetails) {
        NatureReclamant nature = natureReclamantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("NatureReclamant not found"));
        nature.setLibNat(natureDetails.getLibNat());
        nature.setCodNat(natureDetails.getCodNat());
        return natureReclamantRepository.save(nature);
    }
    
    public void deleteNatureReclamant(Long id) {
        NatureReclamant nature = natureReclamantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("NatureReclamant not found"));
        natureReclamantRepository.delete(nature);
    }
    
    // ParamGen operations
    public List<ParamGen> getAllParams() {
        return paramGenRepository.findAll();
    }
    
    public Optional<ParamGen> getParamByName(String libParam) {
        return paramGenRepository.findByLibParam(libParam);
    }
    
    public ParamGen createParam(ParamGen param) {
        return paramGenRepository.save(param);
    }
    
    public ParamGen updateParam(Long id, ParamGen paramDetails) {
        ParamGen param = paramGenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parameter not found"));
        param.setLibParam(paramDetails.getLibParam());
        param.setValParam(paramDetails.getValParam());
        return paramGenRepository.save(param);
    }
    
    public void deleteParam(Long id) {
        ParamGen param = paramGenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parameter not found"));
        paramGenRepository.delete(param);
    }
    
    // NotificationType operations
    public List<NotificationType> getAllNotificationTypes() {
        return notificationTypeRepository.findAll();
    }

    public Page<NotificationType> getNotificationTypesPage(int page, int size) {
        return notificationTypeRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
    }
    
    public Optional<NotificationType> getNotificationTypeById(Long id) {
        return notificationTypeRepository.findById(id);
    }
    
    public NotificationType createNotificationType(NotificationType type) {

        if (type.getActive() == null) {
            type.setActive(true);
        }

        return notificationTypeRepository.save(type);
    }
    
    public NotificationType updateNotificationType(Long id, NotificationType details) {

        NotificationType type = notificationTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("NotificationType not found"));

        type.setLibelle(details.getLibelle());

        if (details.getActive() != null) {
            type.setActive(details.getActive());
        }

        return notificationTypeRepository.save(type);
    }
    
    public void deleteNotificationType(Long id) {
        NotificationType type = notificationTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("NotificationType not found"));
        notificationTypeRepository.delete(type);
    }
}
