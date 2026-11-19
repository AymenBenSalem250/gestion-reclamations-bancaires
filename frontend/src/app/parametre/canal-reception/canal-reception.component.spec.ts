import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CanalReceptionPageListComponent } from './canal-reception-page-list.component';

describe('CanalReceptionPageListComponent', () => {
  let component: CanalReceptionPageListComponent;
  let fixture: ComponentFixture<CanalReceptionPageListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CanalReceptionPageListComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CanalReceptionPageListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
