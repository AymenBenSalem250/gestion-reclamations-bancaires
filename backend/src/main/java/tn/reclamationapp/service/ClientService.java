package tn.reclamationapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.reclamationapp.model.Client;
import tn.reclamationapp.repository.ClientRepository;

import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public Optional<Client> findByNumCompte(String numCpt) {
        return clientRepository.findByNumCpt(numCpt);
    }
    public Optional<Client> findByCodeClt(String codeClt) {
        return clientRepository.findById(codeClt);
    }
}