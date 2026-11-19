import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NatureClientPageListComponent } from './nature-client-page-list.component';

describe('NatureClientPageListComponent', () => {
  let component: NatureClientPageListComponent;
  let fixture: ComponentFixture<NatureClientPageListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NatureClientPageListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(NatureClientPageListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
