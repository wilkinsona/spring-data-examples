/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.springdata.jpa.security;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration test to show the usage of Spring Security constructs within Repository query methods.
 * 
 * @author Oliver Gierke
 * @author Thomas Darimont
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SecurityConfiguration.class)
@Transactional
public class SecurityIntegrationTests {

	@Autowired UserRepository userRepository;
	@Autowired BusinessObjectRepository businessObjectRepository;
	@Autowired SecureBusinessObjectRepository secureBusinessObjectRepository;

	User tom;
	User olli;
	User admin;

	BusinessObject object1;
	BusinessObject object2;
	BusinessObject object3;

	@Before
	public void setup(){

		tom = userRepository.save(new User("thomas","darimont","tdarimont@example.org"));
		olli = userRepository.save(new User("oliver","gierke","ogierke@example.org"));
		admin = userRepository.save(new User("admin","admin","admin@example.org"));

		object1 = businessObjectRepository.save(new BusinessObject("object1", olli));
		object2 = businessObjectRepository.save(new BusinessObject("object2", olli));
		object3 = businessObjectRepository.save(new BusinessObject("object3", tom));
	}

	@Test
	public void findBusinessObjectsForCurrentUserShouldReturnOnlyBusinessObjectsWhereCurrentUserIsOwner(){

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(tom,"x"));

		List<BusinessObject> businessObjects = secureBusinessObjectRepository.findBusinessObjectsForCurrentUser();

		assertThat(businessObjects,hasSize(1));
		assertThat(businessObjects,contains(object3));

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(olli,"x"));

		businessObjects = secureBusinessObjectRepository.findBusinessObjectsForCurrentUser();

		assertThat(businessObjects,hasSize(2));
		assertThat(businessObjects,contains(object1,object2));
	}

	@Test
	public void findBusinessObjectsForCurrentUserShouldReturnAllObjectsForAdmin(){

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(admin,"x", Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))));

		List<BusinessObject> businessObjects = secureBusinessObjectRepository.findBusinessObjectsForCurrentUser();

		assertThat(businessObjects,hasSize(3));
		assertThat(businessObjects,contains(object1,object2, object3));
	}
}
