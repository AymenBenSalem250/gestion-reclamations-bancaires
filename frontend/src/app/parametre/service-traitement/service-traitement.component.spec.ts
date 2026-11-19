import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceTraitementPageListComponent } from './service-traitement-page-list.component';

describe('ServiceTraitementPageListComponent', () => {
  let component: ServiceTraitementPageListComponent;
  let fixture: ComponentFixture<ServiceTraitementPageListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ServiceTraitementPageListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ServiceTraitementPageListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
