package backend.proj5.entity;

import jakarta.persistence.*;

@Entity
@Table(name="session_timeout")
@NamedQuery(name="SessionTimeOut.findTimeout", query="SELECT a FROM SessionTimeOutEntity a")
@NamedQuery(name="SessionTimeOut.updateTimeout", query="UPDATE SessionTimeOutEntity a SET a.timeout = :timeout")


public class SessionTimeOutEntity {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable=false, unique = true, updatable = false)
    private int id;

    @Column(name="timeout", nullable=false, unique = true)
    private int timeout;

    public SessionTimeOutEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
