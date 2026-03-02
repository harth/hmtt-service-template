package uk.co.hmtt.template.item;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for {@link Item} entities. */
public interface ItemRepository extends JpaRepository<Item, Long> {}
