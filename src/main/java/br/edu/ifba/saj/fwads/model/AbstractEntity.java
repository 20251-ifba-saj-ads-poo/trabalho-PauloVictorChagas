package br.edu.ifba.saj.fwads.model;

import java.util.UUID;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;

@MappedSuperclass
public abstract class AbstractEntity {

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID id;

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractEntity)) {
            return false; 
        }
        AbstractEntity other = (AbstractEntity) obj;

        if (id != null) {
            return id.equals(other.id);
        }
        return super.equals(other);
    }

    
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    
}