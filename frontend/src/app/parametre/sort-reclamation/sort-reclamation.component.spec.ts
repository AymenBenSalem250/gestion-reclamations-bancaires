import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SortReclamationPageListComponent } from './sort-reclamation-page-list.component';

describe('SortReclamationPageListComponent', () => {
  let component: SortReclamationPageListComponent;
  let fixture: ComponentFixture<SortReclamationPageListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SortReclamationPageListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SortReclamationPageListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
