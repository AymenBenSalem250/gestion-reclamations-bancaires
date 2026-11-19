package tn.reclamationapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reclamationapp.model.Client;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, String> {

    Optional<Client> findByNumCpt(String numCpt);


}