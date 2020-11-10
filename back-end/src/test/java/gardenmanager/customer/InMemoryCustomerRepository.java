package gardenmanager.customer;

import java.util.List;
import java.util.Optional;

import com.ohboywerecamping.domain.Customer;
import gardenmanager.test.InMemoryRepository;

public class InMemoryCustomerRepository extends InMemoryRepository<Customer, String> implements CustomerRepository {
    public InMemoryCustomerRepository() {
        super(Object::toString, Customer::setId, List.of(customer()));
    }

    private static Customer customer() {
        final Customer customer = new Customer();
        customer.setFirstName("George");
        customer.setLastName("Washington");
        customer.setEmail("gwashington@whitehouse.gov");
        customer.setPhone("2025551234");
        return customer;
    }

    @Override
    public Optional<Customer> findByEmail(final String email) {
        return findAll().stream()
                .filter(customer -> customer.getEmail().equals(email))
                .findFirst();
    }
}
