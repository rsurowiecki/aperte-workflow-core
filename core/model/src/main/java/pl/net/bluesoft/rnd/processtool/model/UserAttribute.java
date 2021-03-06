package pl.net.bluesoft.rnd.processtool.model;

//import org.hibernate.annotations.OnDelete;
//import org.hibernate.annotations.OnDeleteAction;

import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Parameter;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pt_user_attribute")
public class UserAttribute extends UserAttributesSupport {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_USER_ATTR")
			}
	)
	@Column(name = "id")
	protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserData user;

    @Column(name="key_")
    private String key;
    @Column(name="value_")
    private String value;

//    @XmlTransient
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private UserAttribute parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<UserAttribute> attributes;

    public UserAttribute() {
    }

    public UserAttribute(String key, String value) {
        this(key, value, null, null);
    }

    public UserAttribute(UserAttribute attribute) {
        this(attribute.getKey(), attribute.getValue(), attribute.getUser(), attribute.getParent());
    }

    public UserAttribute(String key, String value, UserData user) {
        this(key, value, user, null);
    }

    public UserAttribute(String key, String value, UserData user, UserAttribute parent) {
        this.user = user;
        this.key = key;
        this.value = value;
        this.parent = parent;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
    public Set<UserAttribute> getAttributes() {
        return attributes != null ? attributes : (attributes = new HashSet<UserAttribute>());
    }

    public void setAttributes(Set<UserAttribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    @XmlTransient
    public UserData getUser() {
        return user;
    }

//    @XmlTransient
    public void setUser(UserData user) {
        this.user = user;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public UserAttribute getParent() {
        return parent;
    }

    public void setParent(UserAttribute parent) {
        this.parent = parent;
    }
}
