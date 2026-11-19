import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ObjetReclamationPageListComponent } from './objet-reclamation-page-list.component';

describe('ObjetReclamationPageListComponent', () => {
  let component: ObjetReclamationPageListComponent;
  let fixture: ComponentFixture<ObjetReclamationPageListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ObjetReclamationPageListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ObjetReclamationPageListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
